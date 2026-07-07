/* ── Channel marquee ── */
const channels = [
    { name: 'A24 Argentina', logo: 'https://i.imgur.com/LnXQkIU.png' },
    { name: '13C Chile', logo: 'https://i.imgur.com/Zfe2f5j.png' },
    { name: 'ADN 40', logo: 'https://i.imgur.com/Og17U9N.png' },
    { name: 'Al Jazeera', logo: 'https://i.imgur.com/7bRVpnu.png' },
    { name: 'A&E', logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/A%26E_Network_logo.svg/960px-A%26E_Network_logo.svg.png' },
    { name: 'Pluto TV Acapulco', logo: 'https://images.pluto.tv/channels/61a52615cbef2500072876e2/colorLogoPNG_1756972226445.png' },
    { name: 'ABC Teleshow', logo: 'https://i.imgur.com/cF7gJ2Q.jpeg' },
    { name: '3ABN', logo: 'https://i.imgur.com/IecOZHR.png' },
];
const marquee = document.getElementById('channelMarquee');
if (marquee) {
    const items = [...channels, ...channels].map(c =>
        `<div class="chip"><img src="${c.logo}" alt="" loading="lazy"><span>${c.name}</span></div>`
    ).join('');
    marquee.innerHTML = items;
}

/* ── Auth ── */
function handleAuth(e) {
    e.preventDefault();
    const email = document.getElementById('authEmail').value;
    const pass = document.getElementById('authPass').value;
    fetch('https://apitvxargtec.duckdns.org/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password: pass })
    })
    .then(r => r.json())
    .then(data => {
        if (data.token) {
            alert(t('authSuccess'));
        } else {
            alert('❌ ' + (data.message || t('invalidCreds')));
        }
    })
    .catch(() => alert('❌ ' + t('connError')));
    return false;
}

/* ── Activation 120s ── */
let timerInterval = null;
let timeLeft = 120;
let currentCode = '';

function fmtTime(secs) {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
}

function fetchCodeFromBackend() {
    return fetch('https://apitvxargtec.duckdns.org/api/activation/code')
        .then(r => r.json())
        .then(data => {
            if (data.data && data.data.code) {
                currentCode = data.data.code;
                const el = document.getElementById('activationCode');
                if (el) el.textContent = currentCode;
                timeLeft = data.data.expiresIn || 120;
                updateTimer();
                return currentCode;
            }
            throw new Error('Respuesta inválida del servidor');
        })
        .catch(() => {
            // Generar fallback y registrarlo en backend
            const code = Math.floor(100000 + Math.random() * 900000).toString();
            return fetch('https://apitvxargtec.duckdns.org/api/activation/register-fallback', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({code: code})
            }).then(() => {
                currentCode = code;
                const el = document.getElementById('activationCode');
                if (el) el.textContent = currentCode;
                timeLeft = 120;
                updateTimer();
                return currentCode;
            }).catch(() => {
                const el = document.getElementById('activationCode');
                if (el) el.textContent = 'Error';
                return null;
            });
        });
}

function updateTimer() {
    const el = document.getElementById('timer');
    if (!el) return;
    el.textContent = `⏱ ${fmtTime(timeLeft)}`;
    el.className = 'timer' + (timeLeft <= 15 ? ' warning' : '');
    if (timeLeft <= 0) fetchCodeFromBackend();
}

function openActivation() {
    const modal = document.getElementById('activationModal');
    if (modal) modal.classList.add('active');
    timeLeft = 120;
    clearInterval(timerInterval);
    fetchCodeFromBackend();
    timerInterval = setInterval(() => { timeLeft--; updateTimer(); }, 1000);
}

function closeModal() {
    const modal = document.getElementById('activationModal');
    if (modal) modal.classList.remove('active');
    clearInterval(timerInterval);
}

const activationModal = document.getElementById('activationModal');
if (activationModal) {
    activationModal.addEventListener('click', function(e) {
        if (e.target === this) closeModal();
    });
}

/* ── i18n ── */
const translations = {
    es: {
        navFeatures: 'Features',
        navChannels: 'Canales',
        navPlanes: 'Planes',
        navSeguridad: 'Seguridad',
        navDownload: 'Descargar',
        navVersiones: 'Versiones',
        heroBadge: '✦ 1,895+ canales · 4K · Sin anuncios',
        heroTitle: 'Streaming que<br>rompe esquemas',
        heroDesc: 'La plataforma IPTV más avanzada. Canales en vivo, series, películas y deportes de todo el mundo con calidad 4K y protección antirrastreo.',
        heroBtnApp: '📲 Descargar App',
        heroBtnExplore: 'Explorar →',
        heroStat1: 'Canales en vivo',
        heroStat2: 'Países',
        heroStat3: 'Calidad máxima',
        heroStat4: 'Uptime',
        featTag: '✦ Features',
        featTitle: 'Diseñado para<br>experiencias extremas',
        featSub: 'Cada detalle está optimizado para ofrecerte la mejor experiencia de streaming, con velocidad, seguridad y diseño de primer nivel.',
        feat1Title: '1,895+ Canales en Vivo',
        feat1Desc: 'Desde Argentina hasta Japón. Canales de 90+ países con streams 4K, Full HD y HD. Actualización automática vía IPTV-org.',
        feat2Title: 'Series + Películas',
        feat2Desc: 'Biblioteca en expansión con contenido bajo demanda organizado por categorías.',
        feat3Title: 'Anti-Rastreo',
        feat3Desc: 'Protección FLAG_SECURE, PIN de 6 dígitos, tokens JWT, interceptores con refresh automático. Tu privacidad no se negocia.',
        feat4Title: 'Auto-Update',
        feat4Desc: 'Actualizaciones automáticas desde GitHub. Nunca te pierdas una mejora.',
        feat5Title: 'Multi-dispositivo',
        feat5Desc: 'Android TV, tablets y phones. Interfaz adaptativa con navegación inferior.',
        feat6Title: 'Backend Go + MySQL',
        feat6Desc: 'API propia en Go con MySQL, Nginx reverse proxy, rutas protegidas, favoritos e historial sincronizados en la nube.',
        feat7Title: 'VIP Plans',
        feat7Desc: 'Mensual, Anual y Vitalicio. Pagos vía Stripe con beneficios escalonados.',
        chanTag: '✦ Canales',
        chanTitle: 'Canales que<br>realmente funcionan',
        chanSub: 'Streams verificados de Argentina, Chile, México, España, USA y más. Actualizados constantemente.',
        planTag: '✦ Planes',
        planTitle: 'Elegí tu plan<br>sin compromiso',
        planSub: 'Todos los planes incluyen acceso instantáneo. Cancelá cuando quieras.',
        planLoginTitle: '🔑 Accedé a tu cuenta',
        planLoginDesc: 'Ingresá tus credenciales para desbloquear los planes o activar tu código gratuito.',
        planLoginBtn: 'Ingresar',
        planActivateBtn: '🎫 Activar código gratuito',
        planMonthly: 'Mensual',
        planMonthlyPrice: '$4.99',
        planMonthlyPeriod: '/mes',
        planYearly: 'Anual',
        planYearlyPrice: '$29.99',
        planYearlyPeriod: '/año',
        planLifetime: 'Vitalicio',
        planLifetimePrice: '$49.99',
        planLifetimePeriod: 'pago único',
        planFeat1: 'Acceso a todos los canales',
        planFeat2: 'Calidad HD',
        planFeat3: 'Sin anuncios',
        planFeat4: 'Soporte prioritario',
        planFeat5: 'Todo lo del plan Mensual',
        planFeat6: 'Ahorra 50%',
        planFeat7: 'Calidad 4K',
        planFeat8: 'EPG completo',
        planFeat9: 'Multi-dispositivo',
        planFeat10: 'Pago único',
        planFeat11: 'Actualizaciones gratis',
        planFeat12: 'Acceso de por vida',
        planBtnMonthly: 'Elegir Mensual',
        planBtnYearly: 'Elegir Anual',
        planBtnLifetime: 'Elegir Vitalicio',
        secTag: '🛡 Seguridad',
        secTitle: 'Protegemos tu<br>experiencia',
        secSub: 'Tu privacidad y seguridad son nuestra prioridad. Cada capa está diseñada para mantenerte protegido.',
        sec1Title: 'FLAG_SECURE',
        sec1Desc: 'Bloqueo de capturas de pantalla en toda la app.',
        sec2Title: 'PIN 6 dígitos',
        sec2Desc: 'Control parental con PIN configurable (default 123456).',
        sec3Title: 'JWT + Refresh',
        sec3Desc: 'Tokens con auto-refresh y renovación silenciosa.',
        sec4Title: 'ProGuard / R8',
        sec4Desc: 'Código ofuscado en release. Reverse engineering mitigado.',
        sec5Title: 'Firebase Security',
        sec5Desc: 'Crashlytics, Analytics, Messaging con reglas seguras.',
        sec6Title: 'SSL/TLS',
        sec6Desc: 'Cifrado extremo a extremo vía Nginx + Let\'s Encrypt.',
        dlTag: 'Descargá la app',
        dlDesc: 'Experimentá TVXargtec en tu dispositivo Android. Streaming sin límites, donde sea.',
        dlApk: 'APK Directa',
        dlGitHub: 'GitHub',
        changelogTag: '✦ Versiones',
        changelogTitle: 'Historial de Versiones',
        changelogSub: 'Evolución y mejoras de TVXargtec.',
        changelogCurrent: 'Julio 2026 — Última versión',
        footer: 'Hecho con ❤️ en Argentina.',
        apiStatus: 'API Status',
        modalTitle: '🎫 Activación Gratuita',
        modalDesc: 'Usá este código de 6 dígitos para activar el plan Free en la app. Vence en 2 minutos.',
        modalAuto: 'El código se regenera automáticamente al vencer.',
        authSuccess: '✅ Sesión iniciada. Descargá la app para disfrutar.',
        invalidCreds: 'Credenciales inválidas',
        connError: 'Error de conexión',
    },
    en: {
        navFeatures: 'Features',
        navChannels: 'Channels',
        navPlanes: 'Plans',
        navSeguridad: 'Security',
        navDownload: 'Download',
        navVersiones: 'Versions',
        heroBadge: '✦ 1,895+ channels · 4K · No ads',
        heroTitle: 'Streaming that<br>breaks barriers',
        heroDesc: 'The most advanced IPTV platform. Live channels, series, movies and sports from around the world with 4K quality and anti-tracking protection.',
        heroBtnApp: '📲 Download App',
        heroBtnExplore: 'Explore →',
        heroStat1: 'Live channels',
        heroStat2: 'Countries',
        heroStat3: 'Max quality',
        heroStat4: 'Uptime',
        featTag: '✦ Features',
        featTitle: 'Designed for<br>extreme experiences',
        featSub: 'Every detail is optimized to give you the best streaming experience, with speed, security and top-tier design.',
        feat1Title: '1,895+ Live Channels',
        feat1Desc: 'From Argentina to Japan. Channels from 90+ countries with 4K, Full HD and HD streams. Auto-update via IPTV-org.',
        feat2Title: 'Series + Movies',
        feat2Desc: 'Expanding library with on-demand content organized by categories.',
        feat3Title: 'Anti-Tracking',
        feat3Desc: 'FLAG_SECURE protection, 6-digit PIN, JWT tokens, auto-refresh interceptors. Your privacy is non-negotiable.',
        feat4Title: 'Auto-Update',
        feat4Desc: 'Automatic updates from GitHub. Never miss an improvement.',
        feat5Title: 'Multi-device',
        feat5Desc: 'Android TV, tablets and phones. Adaptive interface with bottom navigation.',
        feat6Title: 'Go + MySQL Backend',
        feat6Desc: 'Custom Go API with MySQL, Nginx reverse proxy, protected routes, cloud-synced favorites and history.',
        feat7Title: 'VIP Plans',
        feat7Desc: 'Monthly, Yearly and Lifetime. Payments via Stripe with tiered benefits.',
        chanTag: '✦ Channels',
        chanTitle: 'Channels that<br>actually work',
        chanSub: 'Verified streams from Argentina, Chile, Mexico, Spain, USA and more. Constantly updated.',
        planTag: '✦ Plans',
        planTitle: 'Choose your plan<br>no commitment',
        planSub: 'All plans include instant access. Cancel anytime.',
        planLoginTitle: '🔑 Access your account',
        planLoginDesc: 'Enter your credentials to unlock plans or activate your free code.',
        planLoginBtn: 'Sign in',
        planActivateBtn: '🎫 Activate free code',
        planMonthly: 'Monthly',
        planMonthlyPrice: '$4.99',
        planMonthlyPeriod: '/mo',
        planYearly: 'Yearly',
        planYearlyPrice: '$29.99',
        planYearlyPeriod: '/yr',
        planLifetime: 'Lifetime',
        planLifetimePrice: '$49.99',
        planLifetimePeriod: 'one-time',
        planFeat1: 'Access to all channels',
        planFeat2: 'HD quality',
        planFeat3: 'No ads',
        planFeat4: 'Priority support',
        planFeat5: 'Everything in Monthly',
        planFeat6: 'Save 50%',
        planFeat7: '4K quality',
        planFeat8: 'Full EPG',
        planFeat9: 'Multi-device',
        planFeat10: 'One-time payment',
        planFeat11: 'Free updates',
        planFeat12: 'Lifetime access',
        planBtnMonthly: 'Choose Monthly',
        planBtnYearly: 'Choose Yearly',
        planBtnLifetime: 'Choose Lifetime',
        secTag: '🛡 Security',
        secTitle: 'We protect your<br>experience',
        secSub: 'Your privacy and security are our priority. Every layer is designed to keep you protected.',
        sec1Title: 'FLAG_SECURE',
        sec1Desc: 'Screenshot blocking throughout the app.',
        sec2Title: '6-digit PIN',
        sec2Desc: 'Parental control with configurable PIN (default 123456).',
        sec3Title: 'JWT + Refresh',
        sec3Desc: 'Tokens with auto-refresh and silent renewal.',
        sec4Title: 'ProGuard / R8',
        sec4Desc: 'Obfuscated code in release builds. Mitigated reverse engineering.',
        sec5Title: 'Firebase Security',
        sec5Desc: 'Crashlytics, Analytics, Messaging with secure rules.',
        sec6Title: 'SSL/TLS',
        sec6Desc: 'End-to-end encryption via Nginx + Let\'s Encrypt.',
        dlTag: 'Download the app',
        dlDesc: 'Experience TVXargtec on your Android device. Unlimited streaming, anywhere.',
        dlApk: 'APK Direct',
        dlGitHub: 'GitHub',
        changelogTag: '✦ Versions',
        changelogTitle: 'Version History',
        changelogSub: 'Evolution and improvements of TVXargtec.',
        changelogCurrent: 'July 2026 — Latest version',
        footer: 'Made with ❤️ in Argentina.',
        apiStatus: 'API Status',
        modalTitle: '🎫 Free Activation',
        modalDesc: 'Use this 6-digit code to activate the Free plan in the app. Expires in 2 minutes.',
        modalAuto: 'The code regenerates automatically when it expires.',
        authSuccess: '✅ Session started. Download the app to enjoy.',
        invalidCreds: 'Invalid credentials',
        connError: 'Connection error',
    },
    pt: {
        navFeatures: 'Recursos',
        navChannels: 'Canais',
        navPlanes: 'Planos',
        navSeguridad: 'Segurança',
        navDownload: 'Baixar',
        navVersiones: 'Versões',
        heroBadge: '✦ 1.895+ canais · 4K · Sem anúncios',
        heroTitle: 'Streaming que<br>quebra barreiras',
        heroDesc: 'A plataforma IPTV mais avançada. Canais ao vivo, séries, filmes e esportes de todo o mundo com qualidade 4K e proteção anti-rastreio.',
        heroBtnApp: '📲 Baixar App',
        heroBtnExplore: 'Explorar →',
        heroStat1: 'Canais ao vivo',
        heroStat2: 'Países',
        heroStat3: 'Máxima qualidade',
        heroStat4: 'Uptime',
        featTag: '✦ Recursos',
        featTitle: 'Projetado para<br>experiências extremas',
        featSub: 'Cada detalhe é otimizado para oferecer a melhor experiência de streaming, com velocidade, segurança e design de primeiro nível.',
        feat1Title: '1.895+ Canais ao Vivo',
        feat1Desc: 'Da Argentina ao Japão. Canais de 90+ países com streams 4K, Full HD e HD. Atualização automática via IPTV-org.',
        feat2Title: 'Séries + Filmes',
        feat2Desc: 'Biblioteca em expansão com conteúdo sob demanda organizado por categorias.',
        feat3Title: 'Anti-Rastreio',
        feat3Desc: 'Proteção FLAG_SECURE, PIN de 6 dígitos, tokens JWT, interceptadores com refresh automático. Sua privacidade não se negocia.',
        feat4Title: 'Auto-Update',
        feat4Desc: 'Atualizações automáticas do GitHub. Nunca perca uma melhoria.',
        feat5Title: 'Multi-dispositivo',
        feat5Desc: 'Android TV, tablets e phones. Interface adaptativa com navegação inferior.',
        feat6Title: 'Backend Go + MySQL',
        feat6Desc: 'API própria em Go com MySQL, Nginx reverse proxy, rotas protegidas, favoritos e histórico sincronizados na nuvem.',
        feat7Title: 'Planos VIP',
        feat7Desc: 'Mensal, Anual e Vitalício. Pagamentos via Stripe com benefícios escalonados.',
        chanTag: '✦ Canais',
        chanTitle: 'Canais que<br>realmente funcionam',
        chanSub: 'Streams verificados da Argentina, Chile, México, Espanha, EUA e mais. Atualizados constantemente.',
        planTag: '✦ Planos',
        planTitle: 'Escolha seu plano<br>sem compromisso',
        planSub: 'Todos os planos incluem acesso instantâneo. Cancele quando quiser.',
        planLoginTitle: '🔑 Acesse sua conta',
        planLoginDesc: 'Insira suas credenciais para desbloquear os planos ou ativar seu código gratuito.',
        planLoginBtn: 'Entrar',
        planActivateBtn: '🎫 Ativar código gratuito',
        planMonthly: 'Mensal',
        planMonthlyPrice: '$4.99',
        planMonthlyPeriod: '/mês',
        planYearly: 'Anual',
        planYearlyPrice: '$29.99',
        planYearlyPeriod: '/ano',
        planLifetime: 'Vitalício',
        planLifetimePrice: '$49.99',
        planLifetimePeriod: 'pagamento único',
        planFeat1: 'Acesso a todos os canais',
        planFeat2: 'Qualidade HD',
        planFeat3: 'Sem anúncios',
        planFeat4: 'Suporte prioritário',
        planFeat5: 'Tudo do plano Mensal',
        planFeat6: 'Economize 50%',
        planFeat7: 'Qualidade 4K',
        planFeat8: 'EPG completo',
        planFeat9: 'Multi-dispositivo',
        planFeat10: 'Pagamento único',
        planFeat11: 'Atualizações grátis',
        planFeat12: 'Acesso vitalício',
        planBtnMonthly: 'Escolher Mensal',
        planBtnYearly: 'Escolher Anual',
        planBtnLifetime: 'Escolher Vitalício',
        secTag: '🛡 Segurança',
        secTitle: 'Protegemos sua<br>experiência',
        secSub: 'Sua privacidade e segurança são nossa prioridade. Cada camada é projetada para mantê-lo protegido.',
        sec1Title: 'FLAG_SECURE',
        sec1Desc: 'Bloqueio de capturas de tela em todo o app.',
        sec2Title: 'PIN 6 dígitos',
        sec2Desc: 'Controle parental com PIN configurável (padrão 123456).',
        sec3Title: 'JWT + Refresh',
        sec3Desc: 'Tokens com auto-refresh e renovação silenciosa.',
        sec4Title: 'ProGuard / R8',
        sec4Desc: 'Código ofuscado em release. Reverse engineering mitigado.',
        sec5Title: 'Firebase Security',
        sec5Desc: 'Crashlytics, Analytics, Messaging com regras seguras.',
        sec6Title: 'SSL/TLS',
        sec6Desc: 'Criptografia ponta a ponta via Nginx + Let\'s Encrypt.',
        dlTag: 'Baixe o app',
        dlDesc: 'Experimente o TVXargtec no seu dispositivo Android. Streaming sem limites, onde quiser.',
        dlApk: 'APK Direta',
        dlGitHub: 'GitHub',
        changelogTag: '✦ Versões',
        changelogTitle: 'Histórico de Versões',
        changelogSub: 'Evolução e melhorias do TVXargtec.',
        changelogCurrent: 'Julho 2026 — Última versão',
        footer: 'Feito com ❤️ na Argentina.',
        apiStatus: 'Status da API',
        modalTitle: '🎫 Ativação Gratuita',
        modalDesc: 'Use este código de 6 dígitos para ativar o plano Free no app. Expira em 2 minutos.',
        modalAuto: 'O código regenera automaticamente ao expirar.',
        authSuccess: '✅ Sessão iniciada. Baixe o app para aproveitar.',
        invalidCreds: 'Credenciais inválidas',
        connError: 'Erro de conexão',
    }
};

let currentLang = localStorage.getItem('tvxargtec_lang') || 'es';

function t(key) {
    return translations[currentLang]?.[key] || translations['es']?.[key] || key;
}

function toggleLang(e) {
    e.stopPropagation();
    document.getElementById('langMenu').classList.toggle('open');
}

function setLang(lang) {
    if (!translations[lang]) return;
    currentLang = lang;
    localStorage.setItem('tvxargtec_lang', lang);
    const names = { es: 'ES', en: 'EN', pt: 'PT' };
    const flags = { es: 'img/lang/es.svg', en: 'img/lang/en.svg', pt: 'img/lang/pt.svg' };
    document.querySelector('.lang-current').innerHTML = `<img src="${flags[lang]}" alt="" class="flag-icon">${names[lang]} ▾`;
    document.querySelectorAll('.lang-opt').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.lang === lang);
    });
    document.getElementById('langMenu').classList.remove('open');
    document.documentElement.lang = lang === 'en' ? 'en' : lang === 'pt' ? 'pt' : 'es';
    applyTranslations();
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('.lang-dropdown')) {
        document.getElementById('langMenu').classList.remove('open');
    }
});

function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.dataset.i18n;
        const val = t(key);
        if (val) el.innerHTML = val;
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        const key = el.dataset.i18nPlaceholder;
        const val = t(key);
        if (val) el.placeholder = val;
    });
    // Update pricing popular badge
    const popularBadge = document.querySelector('.pricing-card.popular::before');
    if (popularBadge) {
        // CSS pseudo-elements can't be updated via JS, skip
    }
    // Update modal
    const modalTitle = document.querySelector('.modal h2');
    if (modalTitle) modalTitle.innerHTML = t('modalTitle');
    const modalDesc = document.querySelector('.modal > p');
    if (modalDesc) modalDesc.textContent = t('modalDesc');
    const modalAuto = document.querySelector('.modal p:last-of-type');
    if (modalAuto) modalAuto.textContent = t('modalAuto');
    if (typeof twemoji !== 'undefined') {
        twemoji.parse(document.body, { folder: 'svg', ext: '.svg' });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    setLang(currentLang);
    if (typeof twemoji !== 'undefined') {
        twemoji.parse(document.body, { folder: 'svg', ext: '.svg' });
    }
});
