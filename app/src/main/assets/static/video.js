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
        
        const items=["/storage/emulated/0/Videos/Favourite/1290552_720x406_500k.mp4", "/storage/emulated/0/Videos/Favourite/1504904_720x406_500k.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_2e24bb6c985d85283c723cdfbc60b346.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_de83eb9e2b5a6efe83cac236b8e5efd1.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_76267c500ea70c9faa719e4ffec41913-1.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_3d25242aa6e08f7d90f03aac205f26d0.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_b6f97024f1d1d9729df66703ef437193-1.mp4", "/storage/emulated/0/Videos/Favourite/Teenage Fantasies Part II (1981).mp4", "/storage/emulated/0/Videos/Favourite/Entrecuisses (1977).mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_a3dd793b1eb8d830dd2ee86481ed60af-1.mp4", "/storage/emulated/0/Videos/Favourite/3138364_720x406_500k.mp4", "/storage/emulated/0/Videos/Favourite/xvideos.com_a5739bee1ce3c66bf4b0f4facf66e67b.mp4", "/storage/emulated/0/Videos/Favourite/美女 (007).mp4", "/storage/emulated/0/Videos/视频/按摩 (4).mp4", "/storage/emulated/0/Videos/视频/迷奸 (3).mp4", "/storage/emulated/0/Videos/视频/癖好 (004).mp4", "/storage/emulated/0/Videos/视频/按摩 (001).mp4", "/storage/emulated/0/Videos/视频/加藤鹰的指爱视频教程.mp4", "/storage/emulated/0/Videos/视频/癖好 (012).mp4", "/storage/emulated/0/Videos/视频/男生必看！男对女口爱毫米级视频教程（高清）.mp4", "/storage/emulated/0/Videos/视频/美女 (11).mp4", "/storage/emulated/0/Videos/视频/癖好 (035).mp4", "/storage/emulated/0/Videos/视频/癖好 (013).mp4", "/storage/emulated/0/Videos/视频/迷奸 (004).mp4", "/storage/emulated/0/Videos/视频/癖好 (019).mp4", "/storage/emulated/0/Videos/视频/按摩 (3).mp4", "/storage/emulated/0/Videos/视频/癖好 (005).mp4", "/storage/emulated/0/Videos/视频/癖好 (021).mp4", "/storage/emulated/0/Videos/视频/足 (016).mp4", "/storage/emulated/0/Videos/视频/强迫 (002).mp4", "/storage/emulated/0/Videos/视频/美女 (20).mp4", "/storage/emulated/0/Videos/视频/美女 (12).mp4", "/storage/emulated/0/Videos/视频/癖好 (022).mp4", "/storage/emulated/0/Videos/视频/美女 (15).mp4", "/storage/emulated/0/Videos/视频/癖好 (025).mp4", "/storage/emulated/0/Videos/视频/美女 (033).mp4", "/storage/emulated/0/Videos/视频/按摩 (002).mp4", "/storage/emulated/0/Videos/视频/美女 (017).mp4", "/storage/emulated/0/Videos/视频/癖好 (017).mp4", "/storage/emulated/0/Videos/视频/癖好 (014).mp4", "/storage/emulated/0/Videos/视频/美女 (030).mp4", "/storage/emulated/0/Videos/视频/美女 (21).mp4", "/storage/emulated/0/Videos/视频/美女 (006).mp4", "/storage/emulated/0/Videos/视频/癖好 (006).mp4", "/storage/emulated/0/Videos/视频/美女 (24).mp4", "/storage/emulated/0/Videos/视频/癖好 (030).mp4", "/storage/emulated/0/Videos/视频/美女 (13).mp4", "/storage/emulated/0/Videos/视频/癖好 (003).mp4", "/storage/emulated/0/Videos/视频/足 (009).mp4", "/storage/emulated/0/Videos/视频/美女 (10).mp4", "/storage/emulated/0/Videos/视频/迷奸 (008).mp4", "/storage/emulated/0/Videos/视频/强迫 (004).mp4", "/storage/emulated/0/Videos/视频/按摩 (1).mp4", "/storage/emulated/0/Videos/视频/足 (005).mp4", "/storage/emulated/0/Videos/视频/迷奸 (010).mp4", "/storage/emulated/0/Videos/视频/癖好 (015).mp4", "/storage/emulated/0/Videos/视频/癖好 (007).mp4", "/storage/emulated/0/Videos/视频/美女 (22).mp4", "/storage/emulated/0/Videos/视频/癖好 (032).mp4", "/storage/emulated/0/Videos/视频/美女 (038).mp4", "/storage/emulated/0/Videos/视频/足 (013).mp4", "/storage/emulated/0/Videos/视频/美女 (032).mp4", "/storage/emulated/0/Videos/视频/美女 (27).mp4", "/storage/emulated/0/Videos/视频/美女 (23).mp4", "/storage/emulated/0/Videos/视频/癖好 (008).mp4", "/storage/emulated/0/Videos/视频/癖好.mp4", "/storage/emulated/0/Videos/视频/足 (019).mp4", "/storage/emulated/0/Videos/视频/美女 (039).mp4", "/storage/emulated/0/Videos/视频/足 (020).mp4", "/storage/emulated/0/Videos/视频/足 (012).mp4", "/storage/emulated/0/Videos/视频/淫 (007).mp4", "/storage/emulated/0/Videos/视频/按摩 (2).mp4", "/storage/emulated/0/Videos/视频/迷奸 (006).mp4", "/storage/emulated/0/Videos/视频/足 (015).mp4", "/storage/emulated/0/Videos/视频/按摩.mp4", "/storage/emulated/0/Videos/视频/按摩 (5).mp4", "/storage/emulated/0/Videos/视频/迷奸 (001).mp4", "/storage/emulated/0/Videos/视频/美女 (020).mp4", "/storage/emulated/0/Videos/视频/美女 (3).mp4", "/storage/emulated/0/Videos/视频/癖好 (020).mp4", "/storage/emulated/0/Videos/视频/美女 (6).mp4", "/storage/emulated/0/Videos/视频/美女 (14).mp4", "/storage/emulated/0/Videos/视频/美女 (012).mp4", "/storage/emulated/0/Videos/视频/美女 (17).mp4", "/storage/emulated/0/Videos/视频/迷奸.mp4", "/storage/emulated/0/Videos/视频/癖好 (018).mp4", "/storage/emulated/0/Videos/视频/癖好 (034).mp4", "/storage/emulated/0/Videos/1388328_720x406_500k.mp4", "/storage/emulated/0/Videos/video.mp4", "/storage/emulated/0/Videos/0twky9F2xrQi7iIQ.mp4", "/storage/emulated/0/Videos/990FV1QVYIAOYCPKO2GNF.mp4", "/storage/emulated/0/Videos/1948166_720x406_500k.mp4", "/storage/emulated/0/Videos/62f355012c1366f7ef9e1b0c0f9545c8-1080p.mp4", "/storage/emulated/0/Videos/8798e09efeca6cc6f44f66ab0b11cba4-1080p.mp4", "/storage/emulated/0/Videos/xvideos.com_c36b8c0eeb32c4fd7777f7cfaeaa365b.mp4", "/storage/emulated/0/Videos/1524784_720x406_500k.mp4", "/storage/emulated/0/Videos/156916_720x406_500k.mp4", "/storage/emulated/0/Videos/xvideos.com_fb647c43b556cee856b76cc852ac4817.mp4", "/storage/emulated/0/Videos/2192234_720x406_500k.mp4", "/storage/emulated/0/Videos/1995286_720x406_500k.mp4", "/storage/emulated/0/Videos/2043514_720x406_500k.mp4", "/storage/emulated/0/Videos/2186840_720x406_500k.mp4", "/storage/emulated/0/Videos/231.mp4", "/storage/emulated/0/Videos/2323850_720x406_500k.mp4", "/storage/emulated/0/Videos/2376650_720x406_500k.mp4", "/storage/emulated/0/Videos/xvideos.com_1073bfb268204326d5aeace64dd8c4c3.mp4", "/storage/emulated/0/Videos/730668_720x406_500k.mp4", "/storage/emulated/0/Videos/1902196_720x406_500k.mp4", "/storage/emulated/0/Videos/xvideos.com_a6e328abf31eec51f22dc6fff6765feb.mp4", "/storage/emulated/0/Videos/xvideos.com_da16be03672e1e228f526cd27fca10ea.mp4", "/storage/emulated/0/Videos/xvideos.com_1c1ef13328309e0db89ed478ecb1bda6.mp4", "/storage/emulated/0/Videos/xvideos.com_ddfbfb41bd0c75355d2d0338bc09d04f.mp4", "/storage/emulated/0/Videos/xvideos.com_46bcf22bd7caa324c13a0b957cdb25c7.mp4", "/storage/emulated/0/Videos/xvideos.com_b089cfccc348b7e7b7be46a1b21d44bb-1.mp4", "/storage/emulated/0/Videos/xvideos.com_2f3a8d0bd17805acdb43bddb64d8148b-1.mp4", "/storage/emulated/0/Videos/xvideos.com_1c3e29291792b56acbd1a9068e577eb2-1.mp4", "/storage/emulated/0/Videos/1992734_720x406_500k.mp4", "/storage/emulated/0/Videos/2447312_720x406_500k.mp4", "/storage/emulated/0/Videos/2195750_720x406_500k.mp4", "/storage/emulated/0/Videos/2336168_720x406_500k.mp4", "/storage/emulated/0/Videos/2186332_720x406_500k.mp4", "/storage/emulated/0/Videos/3138362_720x406_500k.mp4", "/storage/emulated/0/Videos/2057170_720x406_500k.mp4", "/storage/emulated/0/Videos/xvideos.com_a404fc581f4559ec4fc22bf6c281fc8a.mp4", "/storage/emulated/0/Videos/xvideos.com_e25785460777ce2545194ad27588706b-1.mp4"];
        
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