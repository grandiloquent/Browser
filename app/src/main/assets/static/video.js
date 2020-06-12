// https://developer.mozilla.org/en-US/docs/Web/API/HTMLVideoElement

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
        this.video.src = 'trailer.mp4';
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