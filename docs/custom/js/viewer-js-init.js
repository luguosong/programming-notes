// 图片查看
document$.subscribe(async function () {
    // 遍历所有figure标签
    const figures = document.querySelectorAll('figure');
    figures.forEach((figure, index) => {
        new Viewer(figure);
    });
})
