import { useEffect, useId, useRef, useState } from 'react';
import Button from '../common/Button';
import Icon from '../common/Icon';
import Modal from '../common/Modal';

const ACCEPT = ['image/jpeg', 'image/png', 'image/webp'];
const MAX = 10 * 1024 * 1024;

/** 내 이미지 / 공용 샘플 업로드 공용 모달 */
export default function UploadModal({ open, onClose, defaultType = 'PRODUCT', onUpload, title = '내 이미지 업로드', allowTypeChange = true }) {
  const id = useId();
  const inputRef = useRef(null);
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [type, setType] = useState(defaultType);
  const [name, setName] = useState('');
  const [tags, setTags] = useState('');
  const [agree, setAgree] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [dragOver, setDragOver] = useState(false);

  useEffect(() => {
    if (open) {
      setType(defaultType);
      setFile(null);
      setName('');
      setTags('');
      setAgree(false);
      setError(null);
    }
  }, [open, defaultType]);

  useEffect(() => {
    if (!file) {
      setPreview(null);
      return undefined;
    }
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const pick = (f) => {
    if (!f) return;
    if (!ACCEPT.includes(f.type)) return setError('jpg, png, webp 이미지 파일만 업로드할 수 있습니다.');
    if (f.size > MAX) return setError('파일은 10MB 이하만 업로드할 수 있습니다.');
    setError(null);
    setFile(f);
    if (!name) setName(f.name.replace(/\.[^.]+$/, ''));
  };

  const submit = async () => {
    if (!file) return setError('업로드할 이미지를 선택해 주세요.');
    if (!agree) return setError('이미지 사용 권한을 확인해 주세요.');
    setSubmitting(true);
    setError(null);
    try {
      await onUpload({ file, type, title: name.trim(), tags: tags.trim() });
      onClose();
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={() => !submitting && onClose()}
      title={title}
      description="jpg · png · webp, 10MB 이하. 서버에서 긴 변 1600px 로 자동 리사이즈됩니다."
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={submitting}>취소</Button>
          <Button variant="primary" icon="upload" onClick={submit} loading={submitting}>업로드</Button>
        </>
      }
    >
      <div className="d-grid gap-3">
        {allowTypeChange && (
          <div className="px-tabs" role="radiogroup" aria-label="이미지 종류">
            {[
              ['PRODUCT', '상품 이미지'],
              ['MODEL', '모델 이미지'],
            ].map(([v, l]) => (
              <button key={v} type="button" role="radio" aria-checked={type === v} aria-selected={type === v} className="px-tab" onClick={() => setType(v)}>
                {l}
              </button>
            ))}
          </div>
        )}

        <button
          type="button"
          className={`upload-drop ${dragOver ? 'is-over' : ''} ${preview ? 'has-file' : ''}`}
          onClick={() => inputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault();
            setDragOver(true);
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragOver(false);
            pick(e.dataTransfer.files?.[0]);
          }}
          aria-label="이미지 파일 선택"
        >
          {preview ? (
            <img src={preview} alt="업로드할 이미지 미리보기" />
          ) : (
            <span className="upload-drop__empty">
              <Icon name="upload" size={28} />
              <strong>클릭하거나 파일을 끌어다 놓으세요</strong>
              <span>배경이 투명한 PNG 상품 이미지를 쓰면 합성이 더 자연스러워요</span>
            </span>
          )}
        </button>
        <input ref={inputRef} type="file" accept={ACCEPT.join(',')} hidden onChange={(e) => pick(e.target.files?.[0])} />

        <div className="px-field">
          <label className="px-label" htmlFor={`${id}-title`}>이름</label>
          <input id={`${id}-title`} className="px-input" value={name} maxLength={100} onChange={(e) => setName(e.target.value)} placeholder="예: 봄 시즌 수분 크림" />
        </div>
        <div className="px-field">
          <label className="px-label" htmlFor={`${id}-tags`}>태그 (쉼표로 구분, 검색에 사용)</label>
          <input id={`${id}-tags`} className="px-input" value={tags} maxLength={300} onChange={(e) => setTags(e.target.value)} placeholder="스킨케어, 크림" />
        </div>

        <label className="upload-consent">
          <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
          <span>
            <strong>사용 권한이 있는 이미지만 업로드하세요.</strong> 타인의 사진·연예인 사진 등 저작권/초상권 허락을 받지 않은 이미지는 업로드할 수 없습니다.
          </span>
        </label>
        {error && <p className="px-field-error" role="alert">{error}</p>}
      </div>
    </Modal>
  );
}
