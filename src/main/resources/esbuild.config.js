const esbuildSvelte = require('esbuild-svelte');
const svelteConfig = require('./svelte.config');

module.exports = {
    entryPoints: ['./dev/index.js', ],
    format: 'esm',
    minify: true,
    bundle: true,
    splitting: false,//off because of https://github.com/evanw/esbuild/issues/608
    outdir: './dist',
    sourcemap: 'external',
    plugins: [esbuildSvelte({
        preprocess: svelteConfig.preprocess,
    })],
};
