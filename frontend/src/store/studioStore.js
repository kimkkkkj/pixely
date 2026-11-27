import { create } from 'zustand';

export const IMAGE_TYPES = { PRODUCT: 'PRODUCT', MODEL: 'MODEL' };

/** 스튜디오 화면 상태: 탭, 선택 슬롯, 요청사항, 현재 생성 작업, 필터 */
export const useStudioStore = create((set) => ({
  tab: IMAGE_TYPES.PRODUCT,
  product: null,
  model: null,
  prompt: '',
  generation: null, // { id, status, resultUrl, errorMessage }
  filter: 'ORIGINAL',

  setTab: (tab) => set({ tab }),
  select: (image) =>
    set((s) => {
      const key = image.type === IMAGE_TYPES.PRODUCT ? 'product' : 'model';
      const toggledOff = s[key]?.id === image.id;
      return { [key]: toggledOff ? null : image };
    }),
  clearSlot: (type) => set(type === IMAGE_TYPES.PRODUCT ? { product: null } : { model: null }),
  setPrompt: (prompt) => set({ prompt }),
  setGeneration: (generation) => set({ generation }),
  setFilter: (filter) => set({ filter }),
  resetResult: () => set({ generation: null, filter: 'ORIGINAL' }),
  loadFromHistory: (g) =>
    set({
      product: g.productImage ? { ...g.productImage, type: IMAGE_TYPES.PRODUCT } : null,
      model: g.modelImage ? { ...g.modelImage, type: IMAGE_TYPES.MODEL } : null,
      prompt: g.prompt || '',
      generation: { id: g.id, status: g.status, resultUrl: g.resultUrl, errorMessage: g.errorMessage },
      filter: 'ORIGINAL',
    }),
}));
