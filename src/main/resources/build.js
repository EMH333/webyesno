const esbuild = require('esbuild');
const esbuildOptions = require('./esbuild.config');
const fs = require('fs');
var path = require('path');

function copyHTML() {
  fs.copyFile('./dev/index.html', './dist/index.html', (err) => {
    if (err) throw err;
  });
}

function copyShoelaceAssets(){
  if (!fs.existsSync("./dist/shoelace/assets/icons")) {
    fs.mkdirSync("./dist/shoelace/assets/icons", { recursive: true });
  }
  fs.copyFile('./node_modules/@shoelace-style/shoelace/dist/assets/icons/exclamation-octagon.svg', './dist/shoelace/assets/icons/exclamation-octagon.svg', (err) => {
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
  copyShoelaceAssets();
  esbuild.serve({
    port: 3000,
    servedir: './dist',
  }, serveOptions).then(server => {
    // Call "stop" on the server when you're done
    //server.stop()
    //process.exit(0)
  })
} else {
  //TODO fix directory structure
  esbuild.build(esbuildOptions).catch(() => process.exit(1))
  copyHTML();
  copyShoelaceAssets();
}



function copyFileSync( source, target ) {

  var targetFile = target;

  // If target is a directory, a new file with the same name will be created
  if ( fs.existsSync( target ) ) {
      if ( fs.lstatSync( target ).isDirectory() ) {
          targetFile = path.join( target, path.basename( source ) );
      }
  }

  fs.writeFileSync(targetFile, fs.readFileSync(source));
}

function copyFolderRecursiveSync( source, target ) {
  var files = [];

  // Check if folder needs to be created or integrated
  var targetFolder = path.join( target, path.basename( source ) );
  if ( !fs.existsSync( targetFolder ) ) {
      fs.mkdirSync( targetFolder );
  }

  // Copy
  if ( fs.lstatSync( source ).isDirectory() ) {
      files = fs.readdirSync( source );
      files.forEach( function ( file ) {
          var curSource = path.join( source, file );
          if ( fs.lstatSync( curSource ).isDirectory() ) {
              copyFolderRecursiveSync( curSource, targetFolder );
          } else {
              copyFileSync( curSource, targetFolder );
          }
      } );
  }
}
