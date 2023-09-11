const path = require("path")
const HtmlWebpackPlugin = require("html-webpack-plugin")
const MiniCssExtractPlugin = require("mini-css-extract-plugin")

module.exports = {
  entry: "./src/index.js",
  output: {
    filename: "js/bundle.js", // 指定打包后的js文件名
    path: path.resolve(__dirname, "dist"), // 指定打包后的文件夹
  },
  mode: "development", // 指定打包的模式。development:开发模式,不会压缩打包后的代码。production:生产模式,会压缩打包后的代码
  plugins: [
    new HtmlWebpackPlugin({
      template: "./public/index.html", // 指定要拷贝的html文件
    }),
    new MiniCssExtractPlugin({
      filename: "css/index.css", // 指定打包后的css文件名
    }),
  ],
  module: {
    rules: [
      {
        test: /\.css$/, // 指定要打包的文件类型
        use: [MiniCssExtractPlugin.loader, "css-loader"], // 指定使用的loader
      },
      {
        test: /\.less$/, // 指定要打包的文件类型
        use: [MiniCssExtractPlugin.loader, "css-loader", "less-loader"], // 指定使用的loader
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i, // 指定要打包的文件类型
        type: "asset", // 指定使用的loader
        generator: {
          filename: "img/[name].[hash:6][ext]", // 指定打包后的文件名
        },
      },
    ],
  },
  devServer: {
    port: 3000, // 指定端口号
    open: true, // 自动打开浏览器
  },
}
