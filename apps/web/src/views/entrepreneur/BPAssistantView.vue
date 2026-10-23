<template>
  <div class="bp-assistant">
    <div class="chat-interface">
      <!-- 对话区域 -->
      <div class="chat-body scrollbar-thin" ref="chatBodyRef">
        <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.role]">
          <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="bubble">
            <div v-if="msg.streaming" class="ai-loading">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
            <div v-else class="bubble-content" v-html="formatContent(msg.content)"></div>
            <div class="bubble-time">{{ msg.timestamp }}</div>
          </div>
        </div>
      </div>

      <!-- 快捷指令 -->
      <div class="quick-prompts">
        <span class="prompt-label">💡 快速指令：</span>
        <el-tag
          v-for="q in quickPrompts"
          :key="q"
          class="prompt-tag"
          effect="plain"
          @click="sendQuickPrompt(q)"
        >{{ q }}</el-tag>
      </div>

      <!-- 输入区 -->
      <div class="chat-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="告诉 AI 助手你想打磨 BP 的哪个部分，比如「帮我优化商业模式部分」"
          @keydown.enter.ctrl="handleSend"
          resize="none"
        />
        <div class="input-actions">
          <el-tooltip content="附加 BP 文件">
            <el-button :icon="UploadFilled" circle />
          </el-tooltip>
          <el-tooltip content="清空对话">
            <el-button :icon="Delete" circle @click="clearChat" />
          </el-tooltip>
          <el-button type="primary" :icon="Promotion" :loading="loading" @click="handleSend">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';
import { UploadFilled, Delete, Promotion } from '@element-plus/icons-vue';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: string;
  streaming?: boolean;
}

const inputText = ref('');
const loading = ref(false);
const chatBodyRef = ref<HTMLElement | null>(null);

const messages = ref<ChatMessage[]>([
  {
    id: '1', role: 'assistant', content: '你好！我是 **BP 打磨助手** 🤖\n\n我可以帮助你：\n- 📝 优化 BP 各章节文案\n- 💼 梳理商业模式逻辑\n- 🎯 突出核心亮点\n- 📊 结构化财务预测\n\n你想从哪里开始？',
    timestamp: formatTime(new Date()),
  },
]);

const quickPrompts = [
  '帮我优化商业模式部分',
  '润色项目简介',
  '完善竞争优势分析',
  '如何突出市场空间',
  '财务预测怎么写',
];

function formatTime(d: Date) {
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

function formatContent(text: string) {
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
    .replace(/- /g, '• ');
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
  });
}

watch(messages, scrollToBottom, { deep: true });

async function handleSend() {
  const text = inputText.value.trim();
  if (!text || loading.value) return;

  messages.value.push({
    id: Date.now().toString(),
    role: 'user',
    content: text,
    timestamp: formatTime(new Date()),
  });
  inputText.value = '';

  loading.value = true;
  // Mock AI 流式回复
  const streamingMsg: ChatMessage = {
    id: (Date.now() + 1).toString(),
    role: 'assistant',
    content: '',
    timestamp: formatTime(new Date()),
    streaming: true,
  };
  messages.value.push(streamingMsg);

  const response = generateMockResponse(text);
  for (let i = 0; i < response.length; i++) {
    await new Promise(r => setTimeout(r, 30));
    streamingMsg.content += response[i];
  }
  streamingMsg.streaming = false;
  loading.value = false;
}

function generateMockResponse(input: string) {
  const responses: Record<string, string> = {
    '商业模式': '关于商业模式部分，我建议你这样组织：\n\n1. **客户画像** — 精准定义你的核心客户是谁，用谁买单这个故事\n2. **价值主张** — 你解决了什么痛点，与竞品相比有何独特性\n3. **收入模型** — 是订阅制、交易佣金还是一次性收费\n4. **成本结构** — 关键成本项在哪里\n\n💡 小贴士：投资人最关心 "谁付钱、为什么付钱、为什么现在付钱" 这三个问题。',
    '简介': '项目简介（Executive Summary）建议遵循这个结构：\n\n- **一句话定位**：是什么 + 给谁用 + 解决什么问题\n- **市场机会**：这个赛道的市场规模和增长性\n- **核心方案**：你的核心产品/服务是什么\n- **竞争优势**：为什么是你\n- **融资需求**：融资金额、用途、出让股份\n\n整体控制在 3-5 段话，不要超过一页。',
  };
  for (const [key, val] of Object.entries(responses)) {
    if (input.includes(key)) return val;
  }
  return `好的，收到你的问题：「${input}」\n\n基于你的项目情况，我建议重点关注以下几点：\n\n1. 确保 BP 逻辑清晰，从痛点到解决方案再到市场验证，形成完整闭环\n2. 用数据说话，尽可能用真实数据支撑论点\n3. 突出核心团队，特别是关键岗位的过往经历\n4. 明确的融资用途和资金使用计划\n\n需要我针对某一个部分详细展开吗？`;
}

function sendQuickPrompt(q: string) {
  inputText.value = q;
  handleSend();
}

function clearChat() {
  messages.value = messages.value.filter(m => m.role === 'system');
}
</script>

<style lang="scss" scoped>
.bp-assistant {
  height: calc(100vh - 120px);
  background: white;
  border-radius: 12px;
  border: 1px solid var(--cft-border);
  display: flex;
  .chat-interface {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  .chat-body {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .chat-message {
    display: flex; gap: 12px;
    &.user { flex-direction: row-reverse; .bubble { background: var(--cft-primary); color: white; border-radius: 16px 16px 4px 16px; } }
    &.assistant .bubble { background: var(--cft-bg); border-radius: 16px 16px 16px 4px; }
    .avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--cft-bg); display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
    .bubble {
      max-width: 70%; padding: 12px 16px;
      .bubble-content { font-size: 14px; line-height: 1.7; }
      .bubble-time { font-size: 11px; opacity: 0.6; margin-top: 6px; }
    }
  }
  .quick-prompts { padding: 12px 24px; border-top: 1px solid var(--cft-border); display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
  .prompt-label { font-size: 13px; color: var(--cft-text-muted); }
  .prompt-tag { cursor: pointer; transition: all 0.2s; &:hover { background: var(--cft-primary); color: white; border-color: var(--cft-primary); } }
  .chat-input { padding: 16px 24px; border-top: 1px solid var(--cft-border); background: var(--cft-bg); }
  .input-actions { display: flex; gap: 8px; margin-top: 8px; justify-content: flex-end; align-items: center; }
}
</style>
