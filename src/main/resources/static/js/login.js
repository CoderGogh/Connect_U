document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('login-form');
    const errorBox = document.getElementById('error-box');
    const loginBtn = document.querySelector('.btn-login');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // 에러 초기화
        errorBox.style.display = 'none';
        errorBox.textContent = '';

        // 로딩 상태
        const originalBtnText = loginBtn.textContent;
        loginBtn.textContent = '로그인 중...';
        loginBtn.disabled = true;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            if (response.ok) {
                const data = await response.json();
                // TODO: 세션/닉네임 저장이 필요하면 여기에서 처리
                window.location.href = '/';
            } else {
                const errorData = await response.json().catch(() => ({}));
                // 서버에서 message 내려주면 그거 우선 사용
                errorBox.textContent =
                    errorData.message ||
                    '로그인에 실패했습니다. 이메일 또는 비밀번호를 다시 확인해주세요.';
                errorBox.style.display = 'block';
            }
        } catch (error) {
            console.error('Error:', error);
            errorBox.textContent = '서버와 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
            errorBox.style.display = 'block';
        } finally {
            loginBtn.textContent = originalBtnText;
            loginBtn.disabled = false;
        }
    });
});
