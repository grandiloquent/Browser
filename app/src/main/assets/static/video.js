// https://developer.mozilla.org/en-US/docs/Web/API/HTMLVideoElement

 const MD5 = function(e) {
           function h(a, b) {
               var c, d, e, f, g;
               e = a & 2147483648;
               f = b & 2147483648;
               c = a & 1073741824;
               d = b & 1073741824;
               g = (a & 1073741823) + (b & 1073741823);
               return c & d ? g ^ 2147483648 ^ e ^ f : c | d ? g & 1073741824 ? g ^ 3221225472 ^ e ^ f : g ^ 1073741824 ^ e ^ f : g ^ e ^ f
           }

           function k(a, b, c, d, e, f, g) {
               a = h(a, h(h(b & c | ~b & d, e), g));
               return h(a << f | a >>> 32 - f, b)
           }

           function l(a, b, c, d, e, f, g) {
               a = h(a, h(h(b & d | c & ~d, e), g));
               return h(a << f | a >>> 32 - f, b)
           }

           function m(a, b, d, c, e, f, g) {
               a = h(a, h(h(b ^ d ^ c, e), g));
               return h(a << f | a >>> 32 - f, b)
           }

           function n(a, b, d, c, e, f, g) {
               a = h(a, h(h(d ^ (b | ~c), e), g));
               return h(a << f | a >>> 32 - f, b)
           }

           function p(a) {
               var b = "",
                   d = "",
                   c;
               for (c = 0; 3 >= c; c++) d = a >>> 8 * c & 255, d = "0" + d.toString(16), b += d.substr(d.length - 2, 2);
               return b
           }
           var f = [],
               q, r, s, t, a, b, c, d;
           e = function(a) {
               a = a.replace(/\r\n/g, "\n");
               for (var b = "", d = 0; d < a.length; d++) {
                   var c = a.charCodeAt(d);
                   128 > c ? b += String.fromCharCode(c) : (127 < c && 2048 > c ? b += String.fromCharCode(c >> 6 | 192) : (b += String.fromCharCode(c >> 12 | 224), b += String.fromCharCode(c >> 6 & 63 | 128)), b += String.fromCharCode(c & 63 | 128))
               }
               return b
           }(e);
           f = function(b) {
               var a, c = b.length;
               a = c + 8;
               for (var d = 16 * ((a - a % 64) / 64 + 1), e = Array(d - 1), f = 0, g = 0; g < c;) a = (g - g % 4) / 4, f = g % 4 * 8, e[a] |= b.charCodeAt(g) << f, g++;
               a = (g - g % 4) / 4;
               e[a] |= 128 << g % 4 * 8;
               e[d - 2] = c << 3;
               e[d - 1] = c >>> 29;
               return e
           }(e);
           a = 1732584193;
           b = 4023233417;
           c = 2562383102;
           d = 271733878;
           for (e = 0; e < f.length; e += 16) q = a, r = b, s = c, t = d, a = k(a, b, c, d, f[e + 0], 7, 3614090360), d = k(d, a, b, c, f[e + 1], 12, 3905402710), c = k(c, d, a, b, f[e + 2], 17, 606105819), b = k(b, c, d, a, f[e + 3], 22, 3250441966), a = k(a, b, c, d, f[e + 4], 7, 4118548399), d = k(d, a, b, c, f[e + 5], 12, 1200080426), c = k(c, d, a, b, f[e + 6], 17, 2821735955), b = k(b, c, d, a, f[e + 7], 22, 4249261313), a = k(a, b, c, d, f[e + 8], 7, 1770035416), d = k(d, a, b, c, f[e + 9], 12, 2336552879), c = k(c, d, a, b, f[e + 10], 17, 4294925233), b = k(b, c, d, a, f[e + 11], 22, 2304563134), a = k(a, b, c, d, f[e + 12], 7, 1804603682), d = k(d, a, b, c, f[e + 13], 12, 4254626195), c = k(c, d, a, b, f[e + 14], 17, 2792965006), b = k(b, c, d, a, f[e + 15], 22, 1236535329), a = l(a, b, c, d, f[e + 1], 5, 4129170786), d = l(d, a, b, c, f[e + 6], 9, 3225465664), c = l(c, d, a, b, f[e + 11], 14, 643717713), b = l(b, c, d, a, f[e + 0], 20, 3921069994), a = l(a, b, c, d, f[e + 5], 5, 3593408605), d = l(d, a, b, c, f[e + 10], 9, 38016083), c = l(c, d, a, b, f[e + 15], 14, 3634488961), b = l(b, c, d, a, f[e + 4], 20, 3889429448), a = l(a, b, c, d, f[e + 9], 5, 568446438), d = l(d, a, b, c, f[e + 14], 9, 3275163606), c = l(c, d, a, b, f[e + 3], 14, 4107603335), b = l(b, c, d, a, f[e + 8], 20, 1163531501), a = l(a, b, c, d, f[e + 13], 5, 2850285829), d = l(d, a, b, c, f[e + 2], 9, 4243563512), c = l(c, d, a, b, f[e + 7], 14, 1735328473), b = l(b, c, d, a, f[e + 12], 20, 2368359562), a = m(a, b, c, d, f[e + 5], 4, 4294588738), d = m(d, a, b, c, f[e + 8], 11, 2272392833), c = m(c, d, a, b, f[e + 11], 16, 1839030562), b = m(b, c, d, a, f[e + 14], 23, 4259657740), a = m(a, b, c, d, f[e + 1], 4, 2763975236), d = m(d, a, b, c, f[e + 4], 11, 1272893353), c = m(c, d, a, b, f[e + 7], 16, 4139469664), b = m(b, c, d, a, f[e + 10], 23, 3200236656), a = m(a, b, c, d, f[e + 13], 4, 681279174), d = m(d, a, b, c, f[e + 0], 11, 3936430074), c = m(c, d, a, b, f[e + 3], 16, 3572445317), b = m(b, c, d, a, f[e + 6], 23, 76029189), a = m(a, b, c, d, f[e + 9], 4, 3654602809), d = m(d, a, b, c, f[e + 12], 11, 3873151461), c = m(c, d, a, b, f[e + 15], 16, 530742520), b = m(b, c, d, a, f[e + 2], 23, 3299628645), a = n(a, b, c, d, f[e + 0], 6, 4096336452), d = n(d, a, b, c, f[e + 7], 10, 1126891415), c = n(c, d, a, b, f[e + 14], 15, 2878612391), b = n(b, c, d, a, f[e + 5], 21, 4237533241), a = n(a, b, c, d, f[e + 12], 6, 1700485571), d = n(d, a, b, c, f[e + 3], 10, 2399980690), c = n(c, d, a, b, f[e + 10], 15, 4293915773), b = n(b, c, d, a, f[e + 1], 21, 2240044497), a = n(a, b, c, d, f[e + 8], 6, 1873313359), d = n(d, a, b, c, f[e + 15], 10, 4264355552), c = n(c, d, a, b, f[e + 6], 15, 2734768916), b = n(b, c, d, a, f[e + 13], 21, 1309151649), a = n(a, b, c, d, f[e + 4], 6, 4149444226), d = n(d, a, b, c, f[e + 11], 10, 3174756917), c = n(c, d, a, b, f[e + 2], 15, 718787259), b = n(b, c, d, a, f[e + 9], 21, 3951481745), a = h(a, q), b = h(b, r), c = h(c, s), d = h(d, t);
           return (p(a) + p(b) + p(c) + p(d)).toLowerCase()
       };

function dumpElementSize(element) {

    const clientRect = element.getClientRects()[0];

    console.log(
        'offsetHeight = ' + element.offsetHeight + '\n' +
        'offsetLeft = ' + element.offsetLeft + '\n' +
        'offsetTop = ' + element.offsetTop + '\n' +
        'offsetWidth = ' + element.offsetWidth + '\n' +
        'clientHeight = ' + element.clientHeight + '\n' +
        'clientLeft = ' + element.clientLeft + '\n' +
        'clientTop = ' + element.clientTop + '\n' +
        'clientWidth = ' + element.clientWidth + '\n' +
        'scrollHeight = ' + element.scrollHeight + '\n' +
        'scrollLeft = ' + element.scrollLeft + '\n' +
        'scrollTop = ' + element.scrollTop + '\n' +
        'scrollWidth = ' + element.scrollWidth + '\n' +
        'clientRect.x = ' + clientRect.x + '\n' +
        'clientRect.y = ' + clientRect.y + '\n' +
        'clientRect.width = ' + clientRect.width + '\n' +
        'clientRect.height = ' + clientRect.height + '\n' +
        'clientRect.top = ' + clientRect.top + '\n' +
        'clientRect.right = ' + clientRect.right + '\n' +
        'clientRect.bottom = ' + clientRect.bottom + '\n' +
        'clientRect.left = ' + clientRect.left + '\n'
    )
}

function dumpMouseEvent(event) {
    console.log(
        'event.offsetX = ' + event.offsetX + '\n' +
        'event.offsetY = ' + event.offsetY + '\n' +
        'event.pageX = ' + event.pageX + '\n' +
        'event.pageY = ' + event.pageY + '\n' +
        'event.screenX = ' + event.screenX + '\n' +
        'event.screenY = ' + event.screenY + '\n');
}

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
        fetch('/api/videos')
            .then(res => res.json())
            .then(items => {
                items.forEach(i => {
                    const object = {};
                    object["title"] = i.substr(i.lastIndexOf('/') + 1);
                    object["href"] = encodeURIComponent(i);
                    object["cover"]="/images/"+MD5(i)+'.jpg'
                    const element = this.createItem(object);
                    this.singleColumn.appendChild(element);
                });
            })

    }

    static formatTime(seconds) {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = Math.round(seconds % 60);
        return [h, m > 9 ? m : (h ? '0' + m : m || '0'), s > 9 ? s : '0' + s]
            .filter(Boolean)
            .join(':');
    }


    playVideo() {
        this.video.play()
            .then(
                () => {

                })
            .catch(err => console.error(err));
    }

    pauseVideo() {
        this.video.pause();
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

    togglePlayButton() {
        if (this.video.paused) {
            this.playVideo();
        } else {
            this.pauseVideo();
        }
        // this.dumpVideoStatus();
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
        compactMediaItemMetadataContent.addEventListener('click', event => {
            event.preventDefault();
            this.video.src = event.currentTarget.getAttribute('href');
        })

        const compactMediaItemHeadline = document.createElement('h4');
        compactMediaItemHeadline.className = 'compact-media-item-headline';
        compactMediaItemMetadataContent.appendChild(compactMediaItemHeadline);
        const spanTitle = document.createElement('span');
        spanTitle.setAttribute('role', 'text');
        compactMediaItemHeadline.appendChild(spanTitle);
        spanTitle.appendChild(document.createTextNode(object["title"]));
        return item;

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

        this.timeSecond.textContent = Player.formatTime(this.video.duration);
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
        this.playerControlPlayPauseIcon.querySelector('svg').replaceChild(
            this.createPlayPath(),
            this.playerControlPlayPauseIcon.querySelector('path'));
    }

    onPlay() {
        console.log('[play]');
        const oldChild = this.playerControlPlayPauseIcon.querySelector('polygon');
        if (oldChild) {
            this.playerControlPlayPauseIcon.querySelector('svg').replaceChild(
                this.createPausePath(), oldChild);
        }
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
        this.timeFirst.textContent = Player.formatTime(this.video.currentTime);
    }

    onVolumechange() {
        console.log('[volumechange]');
    }

    onWaiting() {
        console.log('[waiting]');
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
