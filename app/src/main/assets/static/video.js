// https://developer.mozilla.org/en-US/docs/Web/API/HTMLVideoElement


class Player {
    constructor() {
        this.playerControlPlayPauseIcon =
            document.querySelector('.player-control-play-pause-icon');
        this.playerControlPlayPauseIcon.addEventListener('click', event => {
            this.togglePlayButton();
        });

        this.video = document.querySelector('.html5-main-video');
        //        this.video.src = 'trailer.mp4';
        // this.video.src =
        // 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4';

        this.registerVideoEvents();

        this.timeFirst = document.querySelector('.time-first');
        this.timeSecond = document.querySelector('.time-second');
        this.playerControlsContent =
            document.querySelector('.player-controls-content');
        this.initializeTimeHead();
        // setTimeout(() => {
        //     this.playerControlsContent.setAttribute('hidden', '');
        // }, 3000);


        this.singleColumn = document.querySelector('.single-column');

        this.fetchData();

        this.playerControlOverlay = document.getElementById('player-control-overlay');
        this.playerControlsMiddle = document.querySelector('.player-controls-middle');
        this.playerControlsBottom = document.querySelector('.player-controls-bottom');
        this.playerControlOverlay.className = 'animation-enabled';
        this.playerControlsTop = document.querySelector('.player-controls-top');
        this.progressBarPlayed = document.querySelector('.progress-bar-played');
        this.progressBarPlayheadWrapper = document.querySelector('.progress-bar-playhead-wrapper');
        this.progressBarLoaded = document.querySelector('.progress-bar-loaded');
        this.playerControlOverlay.addEventListener('click', event => {
            this.showController();
            this.hiddenController();
        });
        this.bindNext();

        this.playIndex = 0;
    }

    bindNext() {
        this.buttonNext = document.querySelector('.button-next');
        this.buttonNext.addEventListener('click', evt => {

            if (this.playIndex + 1 >= this.items.length) {
                this.playIndex = 0;
            }
            // http://192.168.0.101:12345
            this.video.src = "/watch?v=" + this.items[this.playIndex++];
            this.playVideo();
        });
    }

    bindRemove() {
        this.buttonRemove = document.querySelector('.icon-disable');

    }

    hiddenController() {
        setTimeout(() => {
            this.playerControlsMiddle.style.display = 'none';
            this.playerControlsBottom.style.display = 'none';
            this.playerControlsTop.style.display = 'none';
        }, 5000);
    }

    showController() {
        this.playerControlsMiddle.removeAttribute('style');
        this.playerControlsBottom.removeAttribute('style');
        this.playerControlsTop.removeAttribute('style');
    }

    fetchData() {
        // http://192.168.0.101:12345
        fetch('/api/videos')
            .then(res => res.json())
            .then(items => {
                items.forEach((i, index) => {
                    const object = {};
                    object["title"] = i.substr(i.lastIndexOf('/') + 1);
                    object["href"] = encodeURIComponent(i);
                    object["cover"] = "/images/" + MD5(i) + '.jpg'
                    const element = this.createItem(object);
                    this.singleColumn.appendChild(element);
                });
                this.items = items;
            })
    }


    createItem(object) {
        const item = document.createElement('div');
        item.className = 'item';
        const compactMediaItem = document.createElement('div');
        compactMediaItem.className = 'compact-media-item';
        item.appendChild(compactMediaItem);
        const compactMediaItemImage = document.createElement('a');
        compactMediaItemImage.className = 'compact-media-item-image';
        compactMediaItemImage.setAttribute('aria-hidden', 'true');
        compactMediaItemImage.setAttribute('href', '/watch?v=' + object["href"]);

        compactMediaItemImage.addEventListener('click', this.onItemClick.bind(this));

        compactMediaItem.appendChild(compactMediaItemImage);
        const videoThumbnailContainerCompact = document.createElement('div');
        videoThumbnailContainerCompact.className = 'video-thumbnail-container-compact center';
        compactMediaItemImage.appendChild(videoThumbnailContainerCompact);
        const cover = document.createElement('div');
        cover.className = 'cover video-thumbnail-img video-thumbnail-bg';
        videoThumbnailContainerCompact.appendChild(cover);
        const coverImage = document.createElement('img');
        coverImage.className = 'cover video-thumbnail-img';
        coverImage.setAttribute('alt', '');
        coverImage.setAttribute('src', object["cover"]);
        coverImage.setAttribute('data-src', object["href"]);
        coverImage.addEventListener('click', evt => {
            fetch('/remove?v=' + evt.currentTarget.getAttribute('data-src'));
            evt.preventDefault();
            evt.stopPropagation();
            item.remove();
        });
        videoThumbnailContainerCompact.appendChild(coverImage);
        const videoThumbnailOverlayBottomGroup = document.createElement('div');
        videoThumbnailOverlayBottomGroup.className = 'video-thumbnail-overlay-bottom-group';
        videoThumbnailContainerCompact.appendChild(videoThumbnailOverlayBottomGroup);
        const thumbnailOverlayTimeStatusRenderer = document.createElement('div');
        thumbnailOverlayTimeStatusRenderer.className = 'thumbnail-overlay-time-status-renderer';
        thumbnailOverlayTimeStatusRenderer.setAttribute('data-style', 'DEFAULT');
        videoThumbnailOverlayBottomGroup.appendChild(thumbnailOverlayTimeStatusRenderer);
        const span = document.createElement('span');
        span.setAttribute('role', 'text');
        thumbnailOverlayTimeStatusRenderer.appendChild(span);
        const compactMediaItemMetadata = document.createElement('div');
        compactMediaItemMetadata.className = 'compact-media-item-metadata';
        compactMediaItemMetadata.setAttribute('data-has-badges', 'false');
        compactMediaItem.appendChild(compactMediaItemMetadata);
        const compactMediaItemMetadataContent = document.createElement('a');
        compactMediaItemMetadataContent.className = 'compact-media-item-metadata-content';
        compactMediaItemMetadataContent.setAttribute('href', '/watch?v=' + object["href"]);

        compactMediaItemMetadata.appendChild(compactMediaItemMetadataContent);
        compactMediaItemMetadataContent.addEventListener('click', this.onItemClick.bind(this));
        const compactMediaItemHeadline = document.createElement('h4');
        compactMediaItemHeadline.className = 'compact-media-item-headline';
        compactMediaItemMetadataContent.appendChild(compactMediaItemHeadline);
        const spanTitle = document.createElement('span');
        spanTitle.setAttribute('role', 'text');
        compactMediaItemHeadline.appendChild(spanTitle);
        spanTitle.appendChild(document.createTextNode(object["title"]));
        return item;
    }

    onItemClick(event) {
        event.preventDefault();
        const href = event.currentTarget.getAttribute('href');
        // this.video.src = "http://192.168.0.101:12345/" + substringAfter(substringAfter(href, ':'), '/')
        this.video.src = href;

        this.playIndex = parseInt(event.currentTarget.getAttribute('data-index'));
        document.title = substringAfterLast(decodeURIComponent(href), '/');
        this.playVideo();
        this.hiddenController();
    }

    createPausePath() {
        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('fill', '#FFFFFF');
        path.setAttribute(
            'd',
            'M14,44.3333333 L23.3333333,44.3333333 L23.3333333,11.6666667 L14,11.6666667 L14,44.3333333 Z M32.6666667,11.6666667 L32.6666667,44.3333333 L42,44.3333333 L42,11.6666667 L32.6666667,11.6666667 Z');
        return path;
    }

    createPlayPath() {
        const polygon =
            document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
        polygon.setAttribute('fill', '#FFFFFF');
        polygon.setAttribute(
            'points', '18.6666667 11.6666667 18.6666667 44.3333333 44.3333333 28');
        return polygon;
    }

    dumpVideoStatus() {
        console.log(
            'audioTracks = ' + this.video.audioTracks + '\n' +
            'autoplay = ' + this.video.autoplay + '\n' +
            'buffered = ' + this.video.buffered + '\n' +
            'controller = ' + this.video.controller + '\n' +
            'controls = ' + this.video.controls + '\n' +
            'controlsList = ' + this.video.controlsList + '\n' +
            'crossOrigin = ' + this.video.crossOrigin + '\n' +
            'currentSrc = ' + this.video.currentSrc + '\n' +
            'currentTime = ' + this.video.currentTime + '\n' +
            'defaultMuted = ' + this.video.defaultMuted + '\n' +
            'defaultPlaybackRate = ' + this.video.defaultPlaybackRate + '\n' +
            'disableRemotePlayback = ' + this.video.disableRemotePlayback + '\n' +
            'duration = ' + this.video.duration + '\n' +
            'ended = ' + this.video.ended + '\n' +
            'error = ' + this.video.error + '\n' +
            'loop = ' + this.video.loop + '\n' +
            'mediaGroup = ' + this.video.mediaGroup + '\n' +
            'mediaKeys = ' + this.video.mediaKeys + '\n' +
            'mozAudioCaptured = ' + this.video.mozAudioCaptured + '\n' +
            'mozFragmentEnd = ' + this.video.mozFragmentEnd + '\n' +
            'mozFrameBufferLength = ' + this.video.mozFrameBufferLength + '\n' +
            'mozSampleRate = ' + this.video.mozSampleRate + '\n' +
            'muted = ' + this.video.muted + '\n' +
            'networkState = ' + this.video.networkState + '\n' +
            'paused = ' + this.video.paused + '\n' +
            'playbackRate = ' + this.video.playbackRate + '\n' +
            'played = ' + this.video.played + '\n' +
            'preload = ' + this.video.preload + '\n' +
            'preservesPitch = ' + this.video.preservesPitch + '\n' +
            'readyState = ' + this.video.readyState + '\n' +
            'seekable = ' + this.video.seekable + '\n' +
            'seeking = ' + this.video.seeking + '\n' +
            'sinkId = ' + this.video.sinkId + '\n' +
            'src = ' + this.video.src + '\n' +
            'srcObject = ' + this.video.srcObject + '\n' +
            'textTracks = ' + this.video.textTracks + '\n' +
            'videoTracks = ' + this.video.videoTracks + '\n' +
            'volume = ' + this.video.volume + '\n')
    }

    initializeTimeHead() {
        this.progressBarLine = document.querySelector('.progress-bar-line');
        this.progressBarLine.addEventListener('click', event => {
            const element = event.target;
            const offset = event.offsetX - element.offsetLeft;
            this.video.currentTime = offset / element.offsetWidth * this.video.duration;
            event.stopPropagation();
            console.log(offset);
            console.log('offset = ' + offset + '\n' +
                'element.offsetWidth = ' + element.offsetWidth + '\n' +
                'this.video.duration = ' + this.video.duration + '\n' +
                offset / element.offsetWidth * this.video.duration)
        });
    }

    onAbort() {
        console.log('[abort]');
    }

    onCanplay() {
        console.log('[canplay]');
    }

    onCanplaythrough() {
        console.log('[canplaythrough]');
    }

    onDurationchange() {
        console.log('[durationchange]');
        console.log(this.video.duration / 1000);
        this.timeSecond.textContent = formatTime(this.video.duration);
    }

    onEmptied() {
        console.log('[emptied]');
    }

    onEnded() {
        console.log('[ended]');
    }

    onError() {
        console.log('[error]');
        console.log(this.video.error);
    }

    onLoadeddata() {
        console.log('[loadeddata]');
    }

    onLoadedmetadata() {
        console.log('[loadedmetadata]');
    }

    onLoadstart() {
        console.log('[loadstart]');
    }

    onPause() {
        console.log('[pause]');

    }

    onPlay() {
        console.log('[play]');

    }

    onPlaying() {
        console.log('[playing]');
    }

    onProgress() {
        console.log('[progress]');
    }

    onRatechange() {
        console.log('[ratechange]');
    }

    onSeeked() {
        console.log('[seeked ]');
    }

    onSeeking() {
        console.log('[seeking]');
    }

    onStalled() {
        console.log('[stalled]');
    }

    onSuspend() {
        console.log('[suspend]');
    }

    onTimeupdate() {
        // console.log('[timeupdate]');
        this.timeFirst.textContent = formatTime(this.video.currentTime);

        const fraction = (this.video.currentTime / this.video.duration) * 100;
        this.progressBarPlayed.style.width = fraction + '%';
        this.progressBarPlayheadWrapper.style.marginLeft = fraction + '%';
        if (this.video.buffered)
            this.progressBarLoaded.style.width = this.video.buffered.end(0) / this.video.duration * 100 + '%';
    }

    onVolumechange() {
        console.log('[volumechange]');
    }

    onWaiting() {
        console.log('[waiting]');
    }

    pauseVideo() {
        this.video.pause();
        const path = this.playerControlPlayPauseIcon.querySelector('path');
        console.log(path);
        this.playerControlPlayPauseIcon.querySelector('svg').replaceChild(
            this.createPlayPath(), path
        );
    }

    playVideo() {
        this.video.play()
            .then(() => {
                const oldChild =
                    this.playerControlPlayPauseIcon
                        .querySelector('polygon[fill]');
                if (oldChild) {
                    this.playerControlPlayPauseIcon.querySelector('svg').replaceChild(
                        this.createPausePath(), oldChild);
                }
            })
            .catch(err => console.error(err));
    }

    registerVideoEvents() {
        this.video.addEventListener('abort', this.onAbort.bind(this));
        this.video.addEventListener('canplay', this.onCanplay.bind(this));
        this.video.addEventListener(
            'canplaythrough', this.onCanplaythrough.bind(this));
        this.video.addEventListener(
            'durationchange', this.onDurationchange.bind(this));
        this.video.addEventListener('emptied', this.onEmptied.bind(this));
        this.video.addEventListener('ended', this.onEnded.bind(this));
        this.video.addEventListener('error', this.onError.bind(this));
        this.video.addEventListener('loadeddata', this.onLoadeddata.bind(this));
        this.video.addEventListener(
            'loadedmetadata', this.onLoadedmetadata.bind(this));
        this.video.addEventListener('loadstart', this.onLoadstart.bind(this));
        this.video.addEventListener('pause', this.onPause.bind(this));
        this.video.addEventListener('play', this.onPlay.bind(this));
        this.video.addEventListener('playing', this.onPlaying.bind(this));
        this.video.addEventListener('progress', this.onProgress.bind(this));
        this.video.addEventListener('ratechange', this.onRatechange.bind(this));
        this.video.addEventListener('seeked ', this.onSeeked.bind(this));
        this.video.addEventListener('seeking', this.onSeeking.bind(this));
        this.video.addEventListener('stalled', this.onStalled.bind(this));
        this.video.addEventListener('suspend', this.onSuspend.bind(this));
        this.video.addEventListener('timeupdate', this.onTimeupdate.bind(this));
        this.video.addEventListener('volumechange', this.onVolumechange.bind(this));
        this.video.addEventListener('waiting', this.onWaiting.bind(this));
    }

    togglePlayButton() {
        if (this.video.paused) {
            this.playVideo();
        } else {
            this.pauseVideo();
        }
        // this.dumpVideoStatus();
    }


}


new Player();

/*
// https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement
Array.from($0.querySelectorAll('dt>a>code')).map(i => {
    const c = i.textContent;
    const f = `on${c.substr(0, 1).toUpperCase()}${c.substr(1)}`;
    // return `${f}(){
    //     console.log('[${c}]');
    // }`
    return `this.video.addEventListener('${c}', this.${f}.bind(this));`
}).join('\n');
Array.from($0.querySelectorAll('dt>a>code')).map(i => {
    const c = i.textContent.substr(i.textContent.lastIndexOf('.') + 1);
    // return `${f}(){
    //     console.log('[${c}]');
    // }`
    return `'${c} = '+this.video.${c}+'\\n'`;
}).join('+\n');
Array.from($0.querySelectorAll('dt>a>code')).filter(i=>i.textContent.indexOf('offset')!=-1).map(i => {
    const c = i.textContent.substr(i.textContent.lastIndexOf('.') + 1);
    // return `${f}(){
    //     console.log('[${c}]');
    // }`
    return `'${c} = '+element.${c}+'\\n'`;
}).join('+\n');
Array.from($0.querySelectorAll('dt>a>code')).filter(i=>/offset|page|screen/.test(i.textContent)).map(i => {
    const c = i.textContent.substr(i.textContent.lastIndexOf('.') + 1);
    // return `${f}(){
    //     console.log('[${c}]');
    // }`
    return `'event.${c} = '+event.${c}+'\\n'`;
}).join('+\n');
Array.from($0.querySelectorAll('dt>a>code')).filter(i=>/client|scroll/.test(i.textContent)).map(i => {
    const c = i.textContent.substr(i.textContent.lastIndexOf('.') + 1);
    // return `${f}(){
    //     console.log('[${c}]');
    // }`
    return `'${c} = '+element.${c}+'\\n'`;
}).join('+\n');
function t() {
    const str = 'offset / element.offsetWidth * this.video.duration';
    const re = /[a-zA-Z.]+/g;
    let m;
    const array=[];
    while ((m = re.exec(str)) !==null) {
        array.push(`'${m[0]} = '+${m[0]}+'\\n'`)
    }
   array.join('+\n')
} t();
*/
(function () {
    const elements = $0.querySelectorAll('tr');
    const buffer = [];
    let maxCount = 0;
    for (const element of elements) {
        const children = Array.from(element.querySelectorAll('td'));
        if (children.length > maxCount) {
            maxCount = children.length;
        }
        const line = children;
        children.map((currentValue, index) => {
            if (index === 0) {
                console.log(currentValue)
                return '`' + currentValue.innerText + '`';
            } else {
                return currentValue.innerText;
            }
        }).join('|');
        buffer.push('|' + line + '|');
    }

    console.log("|---".repeat(maxCount) + '|\n' + buffer.join('\n'));
})();