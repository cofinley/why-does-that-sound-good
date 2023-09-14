module.exports = function (config) {
    var junitOutputDir = process.env.CIRCLE_TEST_REPORTS || "target/junit"

    config.set({
        browsers: ['ChromeHeadless'],
        flags: [
            '--no-sandbox',
            '--disable-web-security',
            '--disable-gpu',
        ],
        browserDisconnectTimeout: 10000,
        browserDisconnectTolerance: 3,
        browserNoActivityTimeout: 60000,
        basePath: 'target',
        files: ['karma-test.js'],
        frameworks: ['cljs-test'],
        plugins: [
            'karma-cljs-test',
            'karma-chrome-launcher',
            'karma-junit-reporter'
        ],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
        args: ['shadow.test.karma.init']
        },

        // the default configuration
        junitReporter: {
        outputDir: junitOutputDir + '/karma', // results will be saved as outputDir/browserName.xml
        outputFile: undefined, // if included, results will be saved as outputDir/browserName/outputFile
        suite: '' // suite will become the package name attribute in xml testsuite element
        }
    })
}
