var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var extractPlugin = new ExtractTextPlugin({
    filename: 'bundle.css'
});

module.exports = {
    entry: [
      './src/main.js'
    ],
    
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'),
        publicPath: '/dist'
    },

    module: {
        rules: [
            {
                test: /\.js$/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: {
                            presets: ['es2015']
                        }
                    }
                ]
            },
            {
                test: /\.less$/,
                use: extractPlugin.extract({
                    use: ['css-loader', 'less-loader']
                })
            }
        ]
    },

    plugins: [
        extractPlugin
    ]
}