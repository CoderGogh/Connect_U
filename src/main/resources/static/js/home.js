document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('search-form');
    const keywordInput = document.getElementById('keyword');
    const sortButtons = document.querySelectorAll('.sort-btn');
    const postEmpty = document.getElementById('post-empty');

    if (searchForm && keywordInput && window.cu) {
        searchForm.addEventListener('submit', (e) => {
            if (!window.cu.validateKeyword(keywordInput.value)) {
                e.preventDefault();
                window.cu.showWarning('검색어는 공백을 제외하고 2자 이상 입력해주세요.');
            }
        });
    }

    sortButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
            sortButtons.forEach((b) => b.classList.remove('active'));
            btn.classList.add('active');
            // TODO: 정렬 옵션을 반영해 게시글 목록 로딩
        });
    });

    if (postEmpty) {
        postEmpty.textContent = '게시글 목록을 불러오려면 스크롤하거나 검색/정렬을 선택하세요.';
    }
});
