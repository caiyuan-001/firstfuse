<template>
  <el-tag :type="resolvedType" :size="size" :effect="effect" :round="round">{{ resolvedLabel }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TagType } from '@chuangrongtong/shared';

interface StatusMapItem { label: string; type: TagType; }

const DEFAULT_MAP: Record<string, StatusMapItem> = {
  // 租户状态
  active: { label: '开通', type: 'success' },
  frozen: { label: '冻结', type: 'danger' },
  trial: { label: '试用', type: 'warning' },
  // 项目审核状态
  pending: { label: '待审核', type: 'warning' },
  pass: { label: '已通过', type: 'success' },
  reject: { label: '已驳回', type: 'danger' },
  // 订单状态
  unpaid: { label: '待支付', type: 'warning' },
  paid: { label: '已支付', type: 'success' },
  refunded: { label: '已退款', type: 'info' },
  cancelled: { label: '已取消', type: 'info' },
  // 路演状态
  upcoming: { label: '即将开始', type: 'primary' },
  ongoing: { label: '进行中', type: 'success' },
  finished: { label: '已结束', type: 'info' },
  cancelled_roadshow: { label: '已取消', type: 'info' },
  // 知识库
  published: { label: '已发布', type: 'success' },
  draft: { label: '草稿', type: 'info' },
};

const props = withDefaults(defineProps<{
  status: string | number;
  labelMap?: Record<string, string>;
  typeMap?: Record<string, TagType>;
  size?: 'large' | 'default' | 'small';
  effect?: 'light' | 'dark' | 'plain';
  round?: boolean;
}>(), {
  size: 'small',
  effect: 'light',
  round: false,
});

const resolvedLabel = computed(() => {
  const s = String(props.status);
  if (props.labelMap?.[s]) return props.labelMap[s];
  return DEFAULT_MAP[s]?.label ?? s;
});

const resolvedType = computed<TagType>(() => {
  const s = String(props.status);
  if (props.typeMap?.[s]) return props.typeMap[s];
  return DEFAULT_MAP[s]?.type ?? 'info';
});
</script>
