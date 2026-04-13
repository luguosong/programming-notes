// 单词/音标发音按钮 —— 使用 Web Speech API 在页面内直接朗读
if (typeof document$ !== 'undefined') {
    document$.subscribe(function () {
    // IPA 音标到代表单词的映射
    var ipaToWord = {
        // 短元音
        'æ': 'cat', 'ɛ': 'bed', 'ɪ': 'sit', 'ɒ': 'hot', 'ʌ': 'cup',
        // 长元音
        'eɪ': 'day', 'iː': 'see', 'aɪ': 'kite', 'oʊ': 'go', 'juː': 'cute',
        // 其他元音
        'ɑː': 'father', 'ɔː': 'door', 'ʊ': 'book', 'aʊ': 'cow', 'ɔɪ': 'boy',
        'ɜːr': 'bird', 'ɑːr': 'car', 'ɔːr': 'horse',
        // 辅音
        'ʃ': 'ship', 'tʃ': 'chip', 'θ': 'think', 'ð': 'this',
        'ʒ': 'vision', 'ŋ': 'sing', 'j': 'yes', 'w': 'we'
    };

    // 使用事件委托，无需重复绑定
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.md-pronounce-btn');
        if (!btn) return;

        e.preventDefault();
        e.stopPropagation();

        var container = btn.closest('.md-pronounce') || btn;
        var word = container.dataset.word || btn.dataset.word || '';
        var ipa = container.dataset.ipa || btn.dataset.ipa || '';
        var lang = container.dataset.lang || btn.dataset.lang || 'en-US';
        var rate = parseFloat(container.dataset.rate || btn.dataset.rate || '0.8');

        // data-ipa 优先：点击音标时朗读代表单词
        if (ipa && ipaToWord[ipa]) {
            word = ipaToWord[ipa];
        }

        if (!word) return;

        // 取消正在进行的朗读
        speechSynthesis.cancel();

        var utterance = new SpeechSynthesisUtterance(word);
        utterance.lang = lang;
        utterance.rate = rate;

        // 播放反馈：按钮短暂变色
        btn.classList.add('md-pronounce--playing');
        utterance.onend = function () {
            btn.classList.remove('md-pronounce--playing');
        };
        utterance.onerror = function () {
            btn.classList.remove('md-pronounce--playing');
        };

        speechSynthesis.speak(utterance);
    });
});
}
