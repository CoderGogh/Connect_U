document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('login-form');
    const errorBox = document.getElementById('error-box');
    const loginBtn = document.querySelector('.btn-login');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        errorBox.style.display = 'none';
        errorBox.textContent = '';

        const originalBtnText = loginBtn.textContent;
        loginBtn.textContent = '로그인 중...';
        loginBtn.disabled = true;

        try {
            const response = await window.cu.apiFetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                await response.json();
                window.location.href = '/';
            } else {
                const errorData = await response.json().catch(() => ({}));
                errorBox.textContent = errorData.message || '로그인에 실패했습니다. 이메일 또는 비밀번호를 다시 확인해주세요.';
                errorBox.style.display = 'block';
            }
        } catch (error) {
            errorBox.textContent = error.message || '서버와 통신 중 오류가 발생했습니다.';
            errorBox.style.display = 'block';
        } finally {
            loginBtn.textContent = originalBtnText;
            loginBtn.disabled = false;
        }
    });
});
