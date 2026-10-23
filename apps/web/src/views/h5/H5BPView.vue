<template>
  <div class="h5-bp">
    <header class="bp-head">
      <div class="head-text">
        <h2>BP 打磨助手</h2>
        <p>AI 帮你把商业计划书讲到投资人心里</p>
      </div>
      <button class="upload-btn" @click="fileRef?.click()">
        <el-icon :size="15"><Upload /></el-icon>
        上传 BP
      </button>
      <input ref="fileRef" type="file" accept=".pdf,.ppt,.pptx,.doc,.docx" hidden @change="onFileChange" />
    </header>

    <div class="msg-list">
      <div v-for="m in messages" :key="m.id" class="msg" :class="m.role">
        <div class="bubble">
          <span>{{ m.text }}</span>
          <span v-if="m.streaming" class="cursor">▌</span>
        </div>
      </div>
      <div v-if="typing" class="msg ai">
        <div class="bubble typing"><i></i><i></i><i></i></div>
      </div>
      <div ref="anchor"></div>
    </div>

    <div class="dock">
      <div class="sug-row">
        <span v-for="s in suggestions" :key="s" class="sug-chip" @click="send(s)">{{ s }}</span>
      </div>
      <div class="input-bar">
        <input v-model.trim="draft" placeholder="描述你的问题，或点击上方快捷问题" @keyup.enter="send()" />
        <button class="send-btn" :disabled="typing || isStreaming" @click="send()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onUnmounted, ref } from 'vue';
import { Upload } from '@element-plus/icons-vue';

interface ChatMessage {
  id: number;
  role: 'ai' | 'user';
  text: string;
  streaming?: boolean;
}

let uid = 0;
const nextId = () => ++uid;

const messages = ref<ChatMessage[]>([
  {
    id: nextId(),
    role: 'ai',
    text: '你好，我是 BP 打磨助手。可以帮你优化项目定位、精炼融资亮点、补强数据，也可以模拟投资人的刁钻提问。试试下面的快捷问题，或直接输入你的困惑。',
  },
]);

const suggestions = ['一句话介绍项目', '优化项目定位', '补充市场数据', '模拟投资人提问'];

const replies: Record<string, string> = {
  一句话介绍项目:
    '试试这个版本：「智能影像诊断 SaaS，让基层医院拥有三甲级的阅片能力——已签约 47 家医院，ARR 620 万，正在 Pre-A 轮融资」。结构是「品类 + 差异化价值 + 已验证数据」，比堆形容词有效得多。',
  优化项目定位:
    '你的定位可以从「AI 医疗影像公司」收窄为「基层医院影像辅助诊断 SaaS」：市场更聚焦、付费方明确（医院影像科预算）、竞争位次清晰。定位收窄反而更容易被记住和推荐。',
  补充市场数据:
    '建议引用三组数据：1）2026 年中国医学影像 AI 市场规模约 120 亿，CAGR 35%；2）基层医院影像医生缺口 6.9 万人；3）你所在细分赛道渗透率不足 8%。数据源标注在脚注，可信度更高。',
  模拟投资人提问:
    '首轮大概率会问这三个问题：①获客成本与回款周期（医院账期长）；②与设备厂商的竞争关系；③影像数据合规。建议在 BP 里各用一页正面回应，不要回避。',
};

const defaultReply =
  '收到。基于你的项目档案，我建议优先打磨「融资亮点」部分：把 47 家签约医院、92% 续费率、620 万 ARR 这三个数字放到第一页。你可以继续问我具体某一部分怎么改，或上传 BP 让我通读全文。';

const draft = ref('');
const typing = ref(false);
const isStreaming = ref(false);
const anchor = ref<HTMLElement>();
const fileRef = ref<HTMLInputElement>();

let typingTimer: ReturnType<typeof setTimeout> | null = null;
let streamTimer: ReturnType<typeof setInterval> | null = null;

function scrollBottom() {
  nextTick(() => anchor.value?.scrollIntoView({ behavior: 'smooth', block: 'end' }));
}

function pushUser(text: string) {
  messages.value.push({ id: nextId(), role: 'user', text });
  scrollBottom();
}

function streamAi(full: string) {
  const id = nextId();
  messages.value.push({ id, role: 'ai', text: '', streaming: true });
  isStreaming.value = true;
  streamTimer = setInterval(() => {
    const m = messages.value.find((x) => x.id === id);
    if (!m) return;
    m.text = full.slice(0, m.text.length + 2);
    scrollBottom();
    if (m.text.length >= full.length) {
      m.streaming = false;
      isStreaming.value = false;
      if (streamTimer) clearInterval(streamTimer);
    }
  }, 24);
}

function send(preset?: string) {
  const text = (preset || draft.value).trim();
  if (!text || typing.value || isStreaming.value) return;
  draft.value = '';
  pushUser(text);
  typing.value = true;
  typingTimer = setTimeout(() => {
    typing.value = false;
    streamAi(replies[text] || defaultReply);
  }, 900);
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file) return;
  pushUser(`已上传 BP：${file.name}`);
  typing.value = true;
  typingTimer = setTimeout(() => {
    typing.value = false;
    streamAi(
      `已通读《${file.name}》（file_id: bp_${Date.now().toString().slice(-6)}）。整体结构完整，但摘要页信息密度过高、财务页缺少关键假设。我从第 3 页开始逐页给你批注优化建议…（演示环境为模拟分析）`,
    );
  }, 1000);
}

onUnmounted(() => {
  if (typingTimer) clearTimeout(typingTimer);
  if (streamTimer) clearInterval(streamTimer);
});
</script>

<style lang="scss" scoped>
.h5-bp {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.bp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 16px 14px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.08), transparent);
  .head-text {
    h2 {
      font-size: 20px;
      font-weight: 700;
      color: var(--cft-text-primary);
      margin: 0 0 4px;
    }
    p {
      font-size: 12px;
      color: var(--cft-text-muted);
      margin: 0;
    }
  }
  .upload-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border: 1px solid var(--cft-primary);
    border-radius: 16px;
    background: #fff;
    color: var(--cft-primary);
    font-size: 13px;
    cursor: pointer;
  }
}
.msg-list {
  flex: 1;
  padding: 6px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.msg {
  display: flex;
  &.user {
    justify-content: flex-end;
    .bubble {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: #fff;
      border-radius: 14px 14px 4px 14px;
    }
  }
  &.ai {
    justify-content: flex-start;
    .bubble {
      background: #fff;
      color: var(--cft-text-primary);
      border-radius: 14px 14px 14px 4px;
      border: 1px solid var(--cft-border);
    }
  }
  .bubble {
    max-width: 82%;
    padding: 11px 14px;
    font-size: 14px;
    line-height: 1.7;
    word-break: break-word;
    white-space: pre-wrap;
    .cursor {
      color: var(--cft-primary);
      animation: blink 0.8s infinite;
    }
  }
  .bubble.typing {
    display: flex;
    gap: 4px;
    padding: 14px 16px;
    i {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--cft-primary);
      animation: ai-bounce 1.2s infinite ease-in-out;
      &:nth-child(2) {
        animation-delay: 0.15s;
      }
      &:nth-child(3) {
        animation-delay: 0.3s;
      }
    }
  }
}
@keyframes ai-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
.dock {
  position: sticky;
  bottom: 0;
  background: var(--cft-bg);
  padding: 10px 16px 14px;
  box-shadow: 0 -6px 16px rgba(15, 23, 42, 0.06);
}
.sug-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-bottom: 10px;
  &::-webkit-scrollbar {
    display: none;
  }
  .sug-chip {
    flex-shrink: 0;
    font-size: 12px;
    padding: 6px 12px;
    border-radius: 14px;
    background: #fff;
    border: 1px solid var(--cft-border);
    color: var(--cft-text-secondary);
    cursor: pointer;
    &:active {
      transform: scale(0.96);
    }
  }
}
.input-bar {
  display: flex;
  gap: 10px;
  input {
    flex: 1;
    min-width: 0;
    height: 44px;
    border: none;
    outline: none;
    background: #fff;
    border-radius: 12px;
    padding: 0 14px;
    font-size: 15px;
    color: var(--cft-text-primary);
    &::placeholder {
      color: #a3aec0;
    }
  }
  .send-btn {
    flex-shrink: 0;
    height: 44px;
    padding: 0 20px;
    border: none;
    border-radius: 12px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}
</style>
