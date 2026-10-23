<template>
  <div class="relation-graph">
    <div class="page-header">
      <h1>企业关系图谱</h1>
      <p>企业间投资、合作、竞争、供应链等关系可视化（前端占位 Demo，静态示例图谱）</p>
    </div>

    <el-row :gutter="16">
      <!-- 左侧控制面板 -->
      <el-col :span="6">
        <div class="cft-card">
          <h3>🔎 搜索节点</h3>
          <el-input
            v-model="searchKeyword"
            placeholder="输入企业/人物名称，回车定位"
            :prefix-icon="Search"
            clearable
            @keyup.enter="locateNode"
          />
          <el-divider />

          <h3>🎛️ 图谱配置</h3>
          <el-form label-position="top">
            <el-form-item label="关系类型">
              <el-checkbox-group v-model="visibleRelations">
                <el-checkbox value="invested_in">投资</el-checkbox>
                <el-checkbox value="competitor">竞争</el-checkbox>
                <el-checkbox value="partner">合作/关联</el-checkbox>
                <el-checkbox value="supplier">供应链</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="节点类型">
              <el-checkbox-group v-model="visibleTypes">
                <el-checkbox value="company">企业</el-checkbox>
                <el-checkbox value="person">人物</el-checkbox>
                <el-checkbox value="fund">基金</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="中心节点">
              <el-select v-model="centerNode" class="w-full">
                <el-option v-for="n in centerOptions" :key="n.id" :label="n.name" :value="n.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="展开层数">
              <el-slider v-model="expandLevels" :min="1" :max="3" show-input />
            </el-form-item>
          </el-form>

          <el-divider />
          <el-button class="w-full" @click="relayout"><el-icon><Refresh /></el-icon>重新布局</el-button>
        </div>

        <!-- 图例 -->
        <div class="cft-card mt-4">
          <h4>📖 图例</h4>
          <div class="legend-item"><span class="dot" style="background:#2563eb"></span> 机构/企业</div>
          <div class="legend-item"><span class="dot" style="background:#f59e0b"></span> 核心人物</div>
          <div class="legend-item"><span class="dot" style="background:#8b5cf6"></span> 基金</div>
          <el-divider />
          <div class="legend-item"><span class="line" style="background:#22c55e"></span> 投资关系</div>
          <div class="legend-item"><span class="line" style="background:#ef4444"></span> 竞争关系</div>
          <div class="legend-item"><span class="line" style="background:#2563eb"></span> 合作/关联</div>
        </div>
      </el-col>

      <!-- 图谱画布 -->
      <el-col :span="18">
        <div class="graph-container cft-card">
          <!-- 控制工具栏 -->
          <div class="graph-toolbar">
            <div class="graph-stats">
              <el-tag size="small">节点 {{ filteredNodes.length }} / {{ allNodes.length }}</el-tag>
              <el-tag size="small">关系 {{ filteredEdges.length }} / {{ allEdges.length }}</el-tag>
              <el-tag v-if="centerNodeName" size="small" type="warning" effect="plain">中心：{{ centerNodeName }} · {{ expandLevels }} 层</el-tag>
            </div>
            <span class="roam-hint">滚轮缩放 · 拖拽平移 · 点击节点查看详情</span>
          </div>

          <!-- ECharts 力导向图 -->
          <v-chart
            :key="layoutKey"
            class="graph-canvas"
            :option="graphOption"
            autoresize
            @click="handleNodeClick"
          />

          <!-- 节点详情面板 -->
          <transition name="slide">
            <div v-if="selectedNode" class="node-detail">
              <div class="nd-header">
                <h4>{{ selectedNode.name }}</h4>
                <el-button link @click="selectedNode = null"><el-icon><Close /></el-icon></el-button>
              </div>
              <el-descriptions :column="1" size="small" border>
                <el-descriptions-item label="类型">{{ typeLabel(selectedNode.type) }}</el-descriptions-item>
                <el-descriptions-item label="关联节点">{{ selectedNode.relations.length }} 个</el-descriptions-item>
                <el-descriptions-item label="最近更新">2026-10-15</el-descriptions-item>
              </el-descriptions>
              <div class="nd-relations">
                <div v-for="r in selectedNode.relations" :key="r.name + r.typeLabel" class="nr-item">
                  <span class="nr-type" :class="r.type">{{ r.typeLabel }}</span>
                  <span class="nr-name">{{ r.name }}</span>
                </div>
                <el-empty v-if="selectedNode.relations.length === 0" description="当前筛选条件下无关联" :image-size="60" />
              </div>
            </div>
          </transition>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Refresh, Close } from '@element-plus/icons-vue';

const searchKeyword = ref('');
const visibleRelations = ref(['invested_in', 'competitor', 'partner', 'supplier']);
const visibleTypes = ref(['company', 'person', 'fund']);
const centerNode = ref('sequoia');
const expandLevels = ref(2);
const layoutKey = ref(0);
const selectedNode = ref<any>(null);

interface GNode { id: string; name: string; type: string; symbolSize: number; category: number }
interface GEdge { source: string; target: string; label: string; type: string; lineStyle: Record<string, any> }

// 静态示例图谱数据（模拟 NebulaGraph 返回结果）
const allNodes: GNode[] = [
  { id: 'sequoia', name: '红杉中国', type: 'company', symbolSize: 60, category: 0 },
  { id: 'sequoia_cap', name: 'Sequoia Capital', type: 'company', symbolSize: 45, category: 0 },
  { id: 'bytedance', name: '字节跳动', type: 'company', symbolSize: 55, category: 0 },
  { id: 'meituan', name: '美团', type: 'company', symbolSize: 50, category: 0 },
  { id: 'pdd', name: '拼多多', type: 'company', symbolSize: 45, category: 0 },
  { id: 'jd', name: '京东', type: 'company', symbolSize: 45, category: 0 },
  { id: 'shen_nanpeng', name: '沈南鹏', type: 'person', symbolSize: 35, category: 1 },
  { id: 'zhang_yiming', name: '张一鸣', type: 'person', symbolSize: 35, category: 1 },
  { id: 'hongshan_fund', name: '红杉资本基金', type: 'fund', symbolSize: 40, category: 2 },
  { id: 'tsinghua_hold', name: '清华控股', type: 'fund', symbolSize: 36, category: 2 },
  { id: 'foxconn', name: '富士康', type: 'company', symbolSize: 42, category: 0 },
  { id: 'smic', name: '中芯国际', type: 'company', symbolSize: 42, category: 0 },
];

const allEdges: GEdge[] = [
  { source: 'sequoia', target: 'sequoia_cap', label: '母公司', type: 'partner', lineStyle: { color: '#2563eb' } },
  { source: 'sequoia', target: 'bytedance', label: '投资', type: 'invested_in', lineStyle: { color: '#22c55e' } },
  { source: 'sequoia', target: 'meituan', label: '投资', type: 'invested_in', lineStyle: { color: '#22c55e' } },
  { source: 'sequoia', target: 'pdd', label: '投资', type: 'invested_in', lineStyle: { color: '#22c55e' } },
  { source: 'sequoia', target: 'jd', label: '投资', type: 'invested_in', lineStyle: { color: '#22c55e' } },
  { source: 'sequoia', target: 'shen_nanpeng', label: '创始人', type: 'partner', lineStyle: { color: '#2563eb' } },
  { source: 'bytedance', target: 'meituan', label: '竞争', type: 'competitor', lineStyle: { color: '#ef4444', type: 'dashed' } },
  { source: 'pdd', target: 'jd', label: '竞争', type: 'competitor', lineStyle: { color: '#ef4444', type: 'dashed' } },
  { source: 'bytedance', target: 'hongshan_fund', label: 'LP', type: 'partner', lineStyle: { color: '#2563eb' } },
  { source: 'hongshan_fund', target: 'sequoia', label: '管理', type: 'partner', lineStyle: { color: '#2563eb' } },
  { source: 'bytedance', target: 'zhang_yiming', label: '创始人', type: 'partner', lineStyle: { color: '#2563eb' } },
  { source: 'tsinghua_hold', target: 'smic', label: '投资', type: 'invested_in', lineStyle: { color: '#22c55e' } },
  { source: 'foxconn', target: 'bytedance', label: '供应链', type: 'supplier', lineStyle: { color: '#8b5cf6' } },
  { source: 'smic', target: 'foxconn', label: '供应链', type: 'supplier', lineStyle: { color: '#8b5cf6' } },
];

const centerOptions = computed(() => allNodes.filter((n) => n.type === 'company'));
const centerNodeName = computed(() => allNodes.find((n) => n.id === centerNode.value)?.name || '');

/** 从中心节点 BFS 展开 N 层，得到可见节点 id 集合 */
const reachableIds = computed(() => {
  const visible = new Set<string>([centerNode.value]);
  let frontier = [centerNode.value];
  const typeFilteredEdges = allEdges.filter((e) => visibleRelations.value.includes(e.type));
  for (let level = 0; level < expandLevels.value; level++) {
    const next: string[] = [];
    for (const e of typeFilteredEdges) {
      if (frontier.includes(e.source) && !visible.has(e.target)) { visible.add(e.target); next.push(e.target); }
      if (frontier.includes(e.target) && !visible.has(e.source)) { visible.add(e.source); next.push(e.source); }
    }
    frontier = next;
  }
  return visible;
});

const filteredNodes = computed(() =>
  allNodes.filter((n) => reachableIds.value.has(n.id) && visibleTypes.value.includes(n.type)),
);

const filteredEdges = computed(() => {
  const ids = new Set(filteredNodes.value.map((n) => n.id));
  return allEdges.filter(
    (e) => visibleRelations.value.includes(e.type) && ids.has(e.source) && ids.has(e.target),
  );
});

const graphOption = computed(() => ({
  tooltip: { formatter: (p: any) => (p.dataType === 'node' ? `${p.data.name}（${typeLabel((p.data as GNode).type)}）` : p.data.label) },
  legend: [{ data: ['机构/企业', '核心人物', '基金'], bottom: 0 }],
  series: [{
    type: 'graph',
    layout: 'force',
    data: filteredNodes.value.map((n) => ({
      ...n,
      symbolSize: n.id === centerNode.value ? n.symbolSize + 14 : n.symbolSize,
      itemStyle: n.id === centerNode.value ? { borderColor: '#1d4ed8', borderWidth: 3 } : undefined,
    })),
    links: filteredEdges.value.map((e) => ({
      source: e.source, target: e.target,
      label: { show: true, formatter: e.label, fontSize: 11 },
      lineStyle: e.lineStyle,
    })),
    categories: [
      { name: '机构/企业', itemStyle: { color: '#2563eb' } },
      { name: '核心人物', itemStyle: { color: '#f59e0b' } },
      { name: '基金', itemStyle: { color: '#8b5cf6' } },
    ],
    roam: true,
    draggable: true,
    force: { repulsion: 420, edgeLength: [90, 160] },
    label: { show: true, fontSize: 12 },
    emphasis: { focus: 'adjacency', lineStyle: { width: 3 } },
  }],
}));

function buildDetail(node: GNode) {
  return {
    ...node,
    relations: allEdges
      .filter((e) => e.source === node.id || e.target === node.id)
      .filter((e) => visibleRelations.value.includes(e.type))
      .map((e) => ({
        name: e.source === node.id
          ? allNodes.find((n) => n.id === e.target)?.name
          : allNodes.find((n) => n.id === e.source)?.name,
        type: e.type,
        typeLabel: e.label,
      })),
  };
}

function handleNodeClick(params: any) {
  if (params.dataType !== 'node') return;
  const node = allNodes.find((n) => n.id === (params.data as GNode).id);
  if (node) selectedNode.value = buildDetail(node);
}

function locateNode() {
  const kw = searchKeyword.value.trim();
  if (!kw) return;
  const node = allNodes.find((n) => n.name.includes(kw));
  if (!node) {
    ElMessage.warning(`未找到「${kw}」相关节点`);
    return;
  }
  if (!visibleTypes.value.includes(node.type)) {
    ElMessage.warning(`「${node.name}」属于「${typeLabel(node.type)}」，请先在左侧勾选该节点类型`);
    return;
  }
  selectedNode.value = buildDetail(node);
  ElMessage.success(`已定位到「${node.name}」`);
}

function relayout() {
  layoutKey.value += 1;
}

function typeLabel(t: string) {
  return ({ company: '机构/企业', person: '核心人物', fund: '基金' } as Record<string, string>)[t] || t;
}
</script>

<style lang="scss" scoped>
.relation-graph {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .graph-container { padding: 0; position: relative; overflow: hidden; min-height: 560px; }
  .graph-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; border-bottom: 1px solid var(--cft-border); .graph-stats { display: flex; gap: 8px; } }
  .roam-hint { font-size: 12px; color: var(--cft-text-muted); }
  .graph-canvas { width: 100%; height: 540px; }
  .node-detail { position: absolute; right: 16px; top: 60px; width: 280px; background: white; border-radius: 10px; border: 1px solid var(--cft-border); box-shadow: 0 8px 24px rgba(0,0,0,0.12); z-index: 10; }
  .nd-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; border-bottom: 1px solid var(--cft-border); h4 { margin: 0; } }
  .nd-relations { padding: 12px 16px; max-height: 200px; overflow-y: auto; }
  .nr-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; .nr-type { font-size: 11px; padding: 2px 6px; border-radius: 4px; color: white; &.invested_in { background: #22c55e; } &.competitor { background: #ef4444; } &.partner { background: #2563eb; } &.supplier { background: #8b5cf6; } } .nr-name { font-size: 13px; } }
  .legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; padding: 4px 0; }
  .dot { width: 12px; height: 12px; border-radius: 50%; }
  .line { width: 24px; height: 3px; border-radius: 2px; }
  .slide-enter-active, .slide-leave-active { transition: all 0.2s; }
  .slide-enter-from, .slide-leave-to { opacity: 0; transform: translateX(20px); }
  .w-full { width: 100%; }
  .mt-4 { margin-top: 16px; }
}
</style>
