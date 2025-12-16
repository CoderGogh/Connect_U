(function () {
    function getCsrf() {
        const tokenTag = document.querySelector('meta[name="_csrf"]');
        const headerTag = document.querySelector('meta[name="_csrf_header"]');
        if (tokenTag && headerTag) {
            return {
                headerName: headerTag.content,
                token: tokenTag.content,
            };
        }
        return null;
    }

    function showWarning(message) {
        const warningEvent = new CustomEvent('cu:warning', { detail: { message } });
        window.dispatchEvent(warningEvent);
        if (!warningEvent.defaultPrevented) {
            alert(message);
        }
    }

    async function apiFetch(input, init = {}) {
        const csrf = getCsrf();
        const options = { ...init };
        options.method = options.method || 'GET';
        options.headers = new Headers(init.headers || {});

        const isFormData = options.body instanceof FormData;
        if (!options.headers.has('Content-Type') && options.body && !isFormData) {
            options.headers.set('Content-Type', 'application/json');
        }

        if (csrf && options.method.toUpperCase() !== 'GET') {
            options.headers.set(csrf.headerName, csrf.token);
        }

        const response = await fetch(input, options);

        if (response.status === 403) {
            const loginPath = document.body.dataset.loginPath || '/login';
            window.location.href = loginPath;
            throw new Error('Forbidden - redirected to login');
        }
        if (response.status === 413) {
            showWarning('이미지 크기가 5MB를 초과했습니다.');
            throw new Error('Payload too large');
        }
        if (response.status >= 500) {
            let message;
            try {
                const data = await response.json();
                message = data && data.message;
            } catch (e) {
                message = null;
            }
            throw new Error(message || '서버 오류가 발생했습니다.');
        }

        return response;
    }

    function buildQuery(params) {
        const search = new URLSearchParams();
        Object.entries(params || {}).forEach(([key, value]) => {
            if (value === undefined || value === null) return;
            search.append(key, value);
        });
        return search.toString();
    }

    function validateKeyword(keyword) {
        const value = (keyword || '').trim();
        const condensed = value.replace(/\s+/g, '');
        return condensed.length >= 2;
    }

    function validateImages(files, maxCount = 5, maxSize = 5 * 1024 * 1024) {
        const result = { valid: true, errors: [], accepted: [] };
        if (!files || files.length === 0) {
            return result;
        }
        if (files.length > maxCount) {
            result.valid = false;
            result.errors.push(`이미지는 최대 ${maxCount}개까지 업로드할 수 있습니다.`);
        }
        Array.from(files).forEach((file) => {
            if (file.size > maxSize) {
                result.valid = false;
                result.errors.push(`${file.name} 파일이 5MB를 초과했습니다.`);
            } else {
                result.accepted.push(file);
            }
        });
        return result;
    }

    window.cu = {
        apiFetch,
        buildQuery,
        validateKeyword,
        validateImages,
        showWarning,
    };
})();
