const esbuild = require('esbuild');
const esbuildOptions = require('./esbuild.config');
const fs = require('fs');

function copyHTML() {
  fs.copyFile('./dev/index.html', './dist/index.html', (err) => {
    if (err) throw err;
  });
}

if (process.argv.length >= 2 && process.argv[2] === "clean") {
  const directory = './dist';

  if (fs.existsSync(directory)) {
    fs.rmdirSync(directory, { recursive: true });
  }

  if (!fs.existsSync(directory)) {
    fs.mkdirSync(directory);
  }
}

if (process.argv.length >= 2 && process.argv[2] === "serve") {
  let serveOptions = esbuildOptions;
  serveOptions.minify = false;
  copyHTML();
  esbuild.serve({
    port: 3000,
  }, serveOptions).then(server => {
    // Call "stop" on the server when you're done
    //server.stop()
    //process.exit(0)
  })
} else {
  //TODO fix directory structure
  esbuild.build(esbuildOptions).catch(() => process.exit(1))
  copyHTML();
}
