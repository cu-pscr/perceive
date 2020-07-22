var recorder;
var stream;
var recordedChunks = []

function startRecording() {
    console.log('==== Start!!')
    stream = remoteVideo.captureStream()
    recorder = new MediaRecorder(stream, {
        mimeType: 'video/webm'
    });
    recorder.addEventListener('dataavailable', function(e) {
        console.log(e)
        if (e.data.size > 0) {
            recordedChunks.push(e.data);
        }
    })
    recorder.start();
}

function stopRecording() {
    recorder.stop();
    download();
}

startRecording();

function download() {
  var blob = new Blob(recordedChunks, {
    type: 'video/webm'
  });
  var url = URL.createObjectURL(blob);
  var a = document.createElement('a');
  document.body.appendChild(a);
  a.style = 'display: none';
  a.href = url;
  a.download = 'test.webm';
  a.click();
  window.URL.revokeObjectURL(url);
}
