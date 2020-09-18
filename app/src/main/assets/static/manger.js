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

function uploadFiles(files) {
    for (let file of files) {
        console.log(file);
        const formData = new FormData();
        formData.append('files', file, file.name);
        fetch("/api/sdcard",{
        method:"POST",
        body:formData});
        }
    }

