function humanFileSize(size) {
    var i = Math.floor( Math.log(size) / Math.log(1024) );
    return ( size / Math.pow(1024, i) ).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
};

document.querySelectorAll('.file-size')
.forEach(e=>{
const length=parseFloat(e.getAttribute('data-length'));
if(length)
e.textContent=humanFileSize(length);
});

var dropZone = document.getElementById('app');

// Optional.   Show the copy icon when dragging over.  Seems to only work for chrome.
dropZone.addEventListener('dragover', function (e) {
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
});

// Get file data on drop
dropZone.addEventListener('drop', function (e) {
    e.stopPropagation();
    e.preventDefault();
    uploadFiles(e.dataTransfer.files);
});

async function uploadFiles(files) {
document.querySelector('.dialog').className='dialog dialog-show';
const dialogContext=document.querySelector('.dialog-content span');
const length=files.length;
let i=1;
    for (let file of files) {
    dialogContext.textContent=`正在上传 (${i++}/${length}) ${file.name} ...`;
        const formData = new FormData();
        formData.append('files', file, file.name);
       const v=document.querySelector('[data-directory]').getAttribute('data-directory');
     try{await  fetch("/api/sdcard"+(v?"?v="+v:''),{
                 method:"POST",
                 body:formData})
                 .then(res=>console.log(res));}catch(e){}
        }
            window.location.reload();
    }

