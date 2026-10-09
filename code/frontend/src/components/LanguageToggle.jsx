import { useLanguage } from '../context/LanguageContext';

export default function LanguageToggle() {
  const { language, setLanguage, t } = useLanguage();

  return (
    <div className="language-toggle" role="group" aria-label={t('เลือกภาษา', 'Select language')}>
      <button
        className={`language-toggle__option${language === 'th' ? ' is-active' : ''}`}
        type="button"
        aria-label={t('ภาษาไทย', 'Thai')}
        aria-pressed={language === 'th'}
        onClick={() => setLanguage('th')}
      >TH</button>
      <button
        className={`language-toggle__option${language === 'en' ? ' is-active' : ''}`}
        type="button"
        aria-label="English"
        aria-pressed={language === 'en'}
        onClick={() => setLanguage('en')}
      >EN</button>
    </div>
  );
}
