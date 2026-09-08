<template>
  <main class="app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <button
        class="panel-toggle sidebar-toggle"
        type="button"
        :aria-label="sidebarCollapsed ? '展开目录' : '收起目录'"
        @click="sidebarCollapsed = !sidebarCollapsed"
      >
        {{ sidebarCollapsed ? '›' : '‹' }}
      </button>

      <template v-if="!sidebarCollapsed">
        <div class="brand-block">
          <span class="brand-mark">C</span>
          <div>
            <h1>CatClass</h1>
            <p>Desktop Preview</p>
          </div>
        </div>

        <nav class="nav-list" aria-label="Primary">
          <button
            v-for="item in navItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: currentView === item.id }"
            type="button"
            @click="setView(item.id)"
          >
            <span>{{ item.label }}</span>
            <span v-if="item.id === 'courses'" class="nav-count">{{ selectedSpace?.courses.length ?? 0 }}</span>
          </button>
        </nav>

        <div class="sidebar-footer">
          <span class="status-dot" :class="{ online: !loadError }"></span>
          <span>{{ loadError ? '本地预览' : '桥接已连接' }}</span>
        </div>
      </template>
    </aside>

    <section class="workspace">
      <header class="toolbar">
        <div>
          <p class="eyebrow">{{ bridgeStatus }}</p>
          <h2>{{ toolbarTitle }}</h2>
          <p class="toolbar-meta">{{ toolbarSubtitle }}</p>
        </div>
        <div class="toolbar-actions">
          <button class="ghost-action" type="button" @click="openCreateSpaceDialog">新建课表</button>
          <button class="primary-action" type="button" @click="reloadSpaces">刷新</button>
        </div>
      </header>

      <section v-if="loadError" class="notice warning">
        {{ loadError }}
      </section>

      <section
        v-if="currentView === 'timetable'"
        class="content-grid"
        :class="{ compact: selectedSpace?.settings?.compactMode, 'editor-collapsed': editorCollapsed }"
      >
        <article class="timetable-panel">
          <div class="panel-heading">
            <div>
              <h3>周视图</h3>
              <p>{{ activeTermLabel }}</p>
            </div>
            <div class="panel-heading-actions">
              <div class="week-mode-toggle" role="group" aria-label="周视图切换">
                <button
                  class="ghost-action small segmented-button"
                  type="button"
                  :class="{ active: isWeekMode(5) }"
                  @click="setWeekMode(5)"
                >
                  5天
                </button>
                <button
                  class="ghost-action small segmented-button"
                  type="button"
                  :class="{ active: isWeekMode(7) }"
                  @click="setWeekMode(7)"
                >
                  7天
                </button>
              </div>
              <span>{{ visibleDayLabels.length }} 天 · {{ selectedSpace?.courses.length ?? 0 }} 门课</span>
            </div>
          </div>

          <div v-if="spaces.length > 1" class="space-tabs" aria-label="Timetable spaces">
            <button
              v-for="space in spaces"
              :key="space.id"
              class="space-tab"
              :class="{ active: selectedSpaceId === space.id }"
              type="button"
              @click="selectSpace(space.id)"
            >
              {{ space.name }}
            </button>
          </div>

          <div v-if="selectedSpace" class="timetable-grid" :style="gridStyle">
            <div class="grid-corner">时间</div>
            <div v-for="day in visibleDayLabels" :key="day.index" class="day-header">
              <span>{{ day.label }}</span>
              <span class="day-date">{{ day.dateLabel }}</span>
            </div>

            <template v-for="period in periods" :key="period.index">
              <div class="period-cell">
                <strong>{{ period.label }}</strong>
                <span>{{ period.startTime }} - {{ period.endTime }}</span>
              </div>
              <div
                v-for="day in visibleDayLabels"
                :key="`${period.index}-${day.index}`"
                class="course-cell"
                :class="{ selected: isCellSelected(day.index, period.index), 'drag-over': isDragTarget(day.index, period.index) }"
                role="button"
                tabindex="0"
                :aria-label="`选择 ${day.label} ${period.label}`"
                @click="selectCell(day.index, period.index)"
                @dragover.prevent="setDragTarget(day.index, period.index)"
                @dragleave="clearDragTarget(day.index, period.index)"
                @drop.prevent="dropCourseOnCell(day.index, period.index)"
                @keydown.enter.prevent="selectCell(day.index, period.index)"
                @keydown.space.prevent="selectCell(day.index, period.index)"
              >
                <button
                  v-for="course in coursesForCell(day.index, period.index)"
                  :key="course.id"
                  class="course-chip"
                  :class="{ active: selectedCourseId === course.id, dragging: draggingCourseId === course.id }"
                  type="button"
                  draggable="true"
                  :style="{ borderColor: course.color, backgroundColor: `${course.color}18` }"
                  @click.stop="selectCourse(course)"
                  @dragstart="startCourseDrag(course, $event)"
                  @dragend="endCourseDrag"
                >
                  <strong>{{ course.title }}</strong>
                  <span>{{ course.location ?? '未设置教室' }}</span>
                </button>
                <span v-if="coursesForCell(day.index, period.index).length === 0" class="cell-placeholder">+</span>
              </div>
            </template>
          </div>

          <div v-else class="empty-state">
            <h3>还没有课表</h3>
            <button class="primary-action" type="button" @click="openCreateSpaceDialog">创建第一张课表</button>
          </div>
        </article>

        <aside class="editor-panel" :class="{ collapsed: editorCollapsed }">
          <button
            class="panel-toggle editor-toggle"
            type="button"
            :aria-label="editorCollapsed ? '展开编辑区' : '收起编辑区'"
            @click="editorCollapsed = !editorCollapsed"
          >
            {{ editorCollapsed ? '‹' : '›' }}
          </button>

          <template v-if="!editorCollapsed">
          <div class="panel-heading compact">
            <div>
              <h3>{{ courseEditorTitle }}</h3>
              <p>{{ selectedCellLabel }}</p>
            </div>
          </div>

          <form class="editor-form" @submit.prevent="saveCourse">
            <label>
              课程名称
              <input v-model.trim="courseForm.title" type="text" placeholder="例如：高等数学" />
            </label>
            <label>
              教师
              <input v-model.trim="courseForm.teacher" type="text" placeholder="可选" />
            </label>
            <label>
              教室
              <input v-model.trim="courseForm.location" type="text" placeholder="可选" />
            </label>

            <div class="form-grid two-columns">
              <label>
                星期
                <select v-model.number="courseForm.dayOfWeek">
                  <option v-for="day in dayOptions" :key="day.index" :value="day.index">{{ day.label }}</option>
                </select>
              </label>
              <label>
                起始节次
                <select v-model.number="courseForm.startPeriod">
                  <option v-for="period in periods" :key="period.index" :value="period.index">{{ period.label }}</option>
                </select>
              </label>
            </div>

            <label>
              连续节数
              <input v-model.number="courseForm.periodCount" type="number" min="1" :max="periods.length" />
            </label>

            <label>
              备注
              <textarea v-model.trim="courseForm.note" rows="3" placeholder="可选"></textarea>
            </label>

            <div class="color-section">
              <span class="field-title">课程颜色</span>
              <div class="color-palette">
                <button
                  v-for="color in colorPalette"
                  :key="color"
                  class="color-swatch"
                  :class="{ selected: courseForm.color === color }"
                  type="button"
                  :aria-label="`选择颜色 ${color}`"
                  :style="{ backgroundColor: color }"
                  @click="courseForm.color = color"
                ></button>
                <input v-model="courseForm.color" class="color-input" type="color" aria-label="自定义课程颜色" />
              </div>
            </div>

            <div class="button-row">
              <button class="primary-action" type="submit" :disabled="!selectedSpace">保存课程</button>
              <button class="ghost-action" type="button" @click="resetCourseForm">清空</button>
              <button class="danger-action" type="button" :disabled="!selectedCourse" @click="deleteSelectedCourse">删除</button>
            </div>
          </form>
          </template>
        </aside>
      </section>

      <section v-else-if="currentView === 'courses'" class="module-layout">
        <article class="module-panel">
          <div class="panel-heading">
            <div>
              <h3>课程</h3>
              <p>{{ selectedSpace?.courses.length ?? 0 }} 门课程</p>
            </div>
            <button class="primary-action" type="button" @click="prepareNewCourse">新增课程</button>
          </div>

          <div class="course-list">
            <article v-for="course in selectedSpace?.courses ?? []" :key="course.id" class="course-card">
              <span class="course-color" :style="{ backgroundColor: course.color }"></span>
              <div>
                <h4>{{ course.title }}</h4>
                <p>{{ formatCourseSummary(course) }}</p>
              </div>
              <div class="course-actions">
                <button class="ghost-action small" type="button" @click="focusCourse(course)">编辑</button>
                <button class="ghost-action small" type="button" @click="selectCourse(course)">选中</button>
              </div>
            </article>
          </div>

          <div v-if="(selectedSpace?.courses.length ?? 0) === 0" class="empty-state inline">
            <h3>暂无课程</h3>
            <button class="primary-action" type="button" @click="prepareNewCourse">添加课程</button>
          </div>
        </article>

        <aside class="development-panel">
          <h3>课程库</h3>
          <p>{{ courseModuleMessage }}</p>
          <div class="button-row vertical">
            <button class="ghost-action" type="button" @click="setCourseModuleMessage('标签、分组和批量编辑正在开发中。')">标签管理</button>
            <button class="ghost-action" type="button" @click="setCourseModuleMessage('课程搜索和冲突检查正在开发中。')">搜索与冲突检查</button>
          </div>
        </aside>
      </section>

      <section v-else-if="currentView === 'import'" class="module-layout">
        <article class="module-panel">
          <div class="panel-heading">
            <div>
              <h3>导入</h3>
              <p>图片课程表识别</p>
            </div>
            <button class="primary-action" type="button" @click="openImportFilePicker">选择图片</button>
          </div>

          <input ref="importFileInput" class="visually-hidden" type="file" accept="image/*" @change="handleImportFile" />
          <div class="development-state">
            <strong>{{ importMessage }}</strong>
            <span>OCR、格线识别和草稿确认界面正在开发中。</span>
          </div>
        </article>

        <aside class="development-panel">
          <h3>导入动作</h3>
          <div class="button-row vertical">
            <button class="ghost-action" type="button" @click="setImportMessage('已进入图片解析队列，识别服务正在开发中。')">模拟识别</button>
            <button class="ghost-action" type="button" @click="setImportMessage('草稿审核面板正在开发中。')">查看草稿</button>
          </div>
        </aside>
      </section>

      <section v-else class="module-layout">
        <article class="module-panel">
          <div class="panel-heading">
            <div>
              <h3>课表设置</h3>
              <p>{{ selectedSpace?.name ?? "暂无课表" }}</p>
            </div>
          </div>

          <div v-if="selectedSpace" class="settings-grid">
            <label>
              课表名称
              <input :value="selectedSpace.name" type="text" @input="updateSpaceName(inputValue($event))" />
            </label>
            <label>
              学年
              <input :value="selectedSpace.academicYear ?? ''" type="text" @input="updateAcademicYear(inputValue($event))" />
            </label>
          </div>

          <div v-if="selectedSpace" class="settings-section">
            <span class="field-title">显示日期</span>
            <div class="day-toggle-grid">
              <label v-for="day in dayOptions" :key="day.index" class="check-tile">
                <input
                  type="checkbox"
                  :checked="isVisibleDay(day.index)"
                  @change="toggleVisibleDay(day.index, inputChecked($event))"
                />
                <span>{{ day.label }}</span>
              </label>
            </div>
          </div>

          <div v-if="selectedSpace" class="settings-section two-toggles">
            <label class="switch-row">
              <input type="checkbox" :checked="selectedSpace.settings?.showWeekend" @change="toggleWeekend(inputChecked($event))" />
              <span>显示周末</span>
            </label>
            <label class="switch-row">
              <input type="checkbox" :checked="selectedSpace.settings?.compactMode" @change="toggleCompactMode(inputChecked($event))" />
              <span>紧凑模式</span>
            </label>
          </div>
        </article>

        <aside class="development-panel time-settings-panel">
          <div class="panel-heading compact">
            <div>
              <h3>节次时间</h3>
              <p>{{ periods.length }} 个节次</p>
            </div>
            <button class="ghost-action small" type="button" @click="addPeriod">新增</button>
          </div>

          <div class="period-editor-list">
            <div v-for="period in periods" :key="period.index" class="period-editor-row">
              <input :value="period.label" type="text" @input="updatePeriod(period.index, 'label', inputValue($event))" />
              <input :value="period.startTime" type="time" @input="updatePeriod(period.index, 'startTime', inputValue($event))" />
              <input :value="period.endTime" type="time" @input="updatePeriod(period.index, 'endTime', inputValue($event))" />
              <button class="icon-action" type="button" :disabled="periods.length <= 1" @click="removePeriod(period.index)">×</button>
            </div>
          </div>
        </aside>
      </section>
    </section>

    <div v-if="isCreateSpaceDialogOpen" class="modal-backdrop" @click.self="closeCreateSpaceDialog">
      <form class="dialog" @submit.prevent="createSpace">
        <div class="panel-heading compact">
          <div>
            <h3>创建课表</h3>
            <p>新的课表会先保存在当前桌面预览中</p>
          </div>
        </div>
        <label>
          课表名称
          <input v-model.trim="spaceForm.name" type="text" placeholder="例如：大二上学期" autofocus />
        </label>
        <label>
          学年
          <input v-model.trim="spaceForm.academicYear" type="text" placeholder="例如：2026-2027" />
        </label>
        <div class="button-row end">
          <button class="ghost-action" type="button" @click="closeCreateSpaceDialog">取消</button>
          <button class="primary-action" type="submit">创建</button>
        </div>
      </form>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import type {
  CourseTemplate,
  EntityMeta,
  Period,
  ScheduleRule,
  SpaceSettings,
  Term,
  TimeTemplate,
  TimetableSpace,
} from "../../../../../packages/contracts/types/catclass";

type ViewId = "timetable" | "courses" | "import" | "settings";
type PeriodField = "label" | "startTime" | "endTime";

interface CourseFormState {
  title: string;
  teacher: string;
  location: string;
  color: string;
  note: string;
  dayOfWeek: number;
  startPeriod: number;
  periodCount: number;
}

interface SpaceFormState {
  name: string;
  academicYear: string;
}

interface SelectedCell {
  dayOfWeek: number;
  periodIndex: number;
}

const dayOptions = [
  { index: 1, label: "周一" },
  { index: 2, label: "周二" },
  { index: 3, label: "周三" },
  { index: 4, label: "周四" },
  { index: 5, label: "周五" },
  { index: 6, label: "周六" },
  { index: 7, label: "周日" },
];

const colorPalette = ["#2563EB", "#0F766E", "#C2410C", "#7C3AED", "#DB2777", "#CA8A04", "#0891B2", "#16A34A"];

const navItems: Array<{ id: ViewId; label: string }> = [
  { id: "timetable", label: "课程表" },
  { id: "courses", label: "课程" },
  { id: "import", label: "导入" },
  { id: "settings", label: "设置" },
];

const sidebarCollapsed = ref(false);
const editorCollapsed = ref(false);
const weekViewMode = ref<5 | 7>(5);
const draggingCourseId = ref<string | null>(null);
const dragTarget = ref<SelectedCell | null>(null);

// 页面核心状态：先在渲染层形成完整交互闭环，再通过 preload 同步给主进程内存仓库。
const currentView = ref<ViewId>("timetable");
const spaces = ref<TimetableSpace[]>([]);
const selectedSpaceId = ref<string | null>(null);
const selectedCourseId = ref<string | null>(null);
const selectedCell = ref<SelectedCell | null>(null);
const loadError = ref<string | null>(null);
const bridgeStatus = ref("Connecting to desktop bridge");
const importFileInput = ref<HTMLInputElement | null>(null);
const importMessage = ref("图片课程表导入正在开发中。");
const courseModuleMessage = ref("课程库、标签和批量编辑正在开发中。");
const isCreateSpaceDialogOpen = ref(false);
const spaceForm = ref<SpaceFormState>({ name: "", academicYear: "2026-2027" });
const courseForm = ref<CourseFormState>(createBlankCourseForm());

const selectedSpace = computed(() => spaces.value.find((space) => space.id === selectedSpaceId.value) ?? spaces.value[0] ?? null);
const activeTerm = computed(() => {
  const space = selectedSpace.value;
  return space?.terms.find((term) => term.id === space.activeTermId) ?? space?.terms[0] ?? null;
});
const selectedCourse = computed(() => selectedSpace.value?.courses.find((course) => course.id === selectedCourseId.value) ?? null);
const periods = computed<Period[]>(() => selectedSpace.value?.timeTemplates[0]?.periods ?? []);
const activeTermLabel = computed(() => activeTerm.value?.name ?? "未设置学期");
const visibleDayLabels = computed(() => {
  const visibleDays = selectedSpace.value?.settings?.visibleDays ?? (weekViewMode.value === 7 ? [1, 2, 3, 4, 5, 6, 7] : [1, 2, 3, 4, 5]);
  const activeDays = visibleDays
    .filter((index) => index >= 1 && index <= 7)
    .sort((left, right) => left - right);
  return activeDays.map((index) => {
    const baseDay = dayOptions.find((day) => day.index === index) ?? { index, label: `第 ${index} 天` };
    return {
      ...baseDay,
      dateLabel: formatWeekDate(index),
    };
  });
});
const gridStyle = computed(() => ({
  gridTemplateColumns: `132px repeat(${visibleDayLabels.value.length}, minmax(132px, 1fr))`,
}));
const toolbarTitle = computed(() => {
  if (currentView.value === "timetable") {
    return selectedSpace.value?.name ?? "课程表工作台";
  }
  const current = navItems.find((item) => item.id === currentView.value);
  return current?.label ?? "CatClass";
});
const toolbarSubtitle = computed(() => {
  const courseCount = selectedSpace.value?.courses.length ?? 0;
  const dayCount = visibleDayLabels.value.length;
  return `${courseCount} 门课程 · ${dayCount} 个显示日 · ${periods.value.length} 个节次`;
});
const courseEditorTitle = computed(() => (selectedCourse.value ? "编辑课程" : "新增课程"));
const selectedCellLabel = computed(() => {
  const rule = selectedCourse.value?.scheduleRules[0];
  if (rule) {
    return `${formatDay(rule.dayOfWeek)} · 第 ${rule.startPeriod} 节开始`;
  }
  if (selectedCell.value) {
    return `${formatDay(selectedCell.value.dayOfWeek)} · 第 ${selectedCell.value.periodIndex} 节`;
  }
  return "选择一个格子或课程";
});

function inputValue(event: Event): string {
  return (event.target as HTMLInputElement).value;
}

function inputChecked(event: Event): boolean {
  return (event.target as HTMLInputElement).checked;
}

function setView(view: ViewId) {
  currentView.value = view;
}

function syncWeekViewModeFromSpace(space: TimetableSpace | null | undefined) {
  weekViewMode.value = space?.settings?.visibleDays?.length === 7 ? 7 : 5;
}

function isWeekMode(mode: 5 | 7): boolean {
  return weekViewMode.value === mode;
}

function setWeekMode(mode: 5 | 7) {
  weekViewMode.value = mode;
  if (selectedSpace.value) {
    const settings = ensureSettings(selectedSpace.value);
    settings.visibleDays = mode === 7 ? [1, 2, 3, 4, 5, 6, 7] : [1, 2, 3, 4, 5];
    settings.showWeekend = mode === 7;
    touchSpace(selectedSpace.value);
  }
}

function selectSpace(spaceId: string) {
  selectedSpaceId.value = spaceId;
  selectedCourseId.value = null;
  selectedCell.value = null;
  courseForm.value = createBlankCourseForm();
  syncWeekViewModeFromSpace(selectedSpace.value);
}

// 课表格子本身就是新增课程的入口：点击空格子后，右侧表单会自动带入星期和节次。
function selectCell(dayOfWeek: number, periodIndex: number) {
  selectedCell.value = { dayOfWeek, periodIndex };
  selectedCourseId.value = null;
  courseForm.value = createBlankCourseForm(selectedCell.value);
}

function isCellSelected(dayOfWeek: number, periodIndex: number): boolean {
  return selectedCell.value?.dayOfWeek === dayOfWeek && selectedCell.value.periodIndex === periodIndex;
}

function coursesForCell(dayOfWeek: number, periodIndex: number): CourseTemplate[] {
  return selectedSpace.value?.courses.filter((course) =>
    course.scheduleRules.some(
      (rule) => rule.dayOfWeek === dayOfWeek && periodIndex >= rule.startPeriod && periodIndex < rule.startPeriod + rule.periodCount,
    ),
  ) ?? [];
}


function startCourseDrag(course: CourseTemplate, event: DragEvent) {
  draggingCourseId.value = course.id;
  dragTarget.value = course.scheduleRules[0]
    ? { dayOfWeek: course.scheduleRules[0].dayOfWeek, periodIndex: course.scheduleRules[0].startPeriod }
    : null;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', course.id);
  }
}

function endCourseDrag() {
  draggingCourseId.value = null;
  dragTarget.value = null;
}

function setDragTarget(dayOfWeek: number, periodIndex: number) {
  if (draggingCourseId.value) {
    dragTarget.value = { dayOfWeek, periodIndex };
  }
}

function clearDragTarget(dayOfWeek: number, periodIndex: number) {
  if (dragTarget.value?.dayOfWeek === dayOfWeek && dragTarget.value.periodIndex === periodIndex) {
    dragTarget.value = null;
  }
}

function isDragTarget(dayOfWeek: number, periodIndex: number): boolean {
  return dragTarget.value?.dayOfWeek === dayOfWeek && dragTarget.value.periodIndex === periodIndex;
}

function dropCourseOnCell(dayOfWeek: number, periodIndex: number) {
  const course = selectedSpace.value?.courses.find((item) => item.id === draggingCourseId.value);
  if (!course) {
    return;
  }
  moveCourse(course, dayOfWeek, periodIndex);
  endCourseDrag();
}

function moveCourse(course: CourseTemplate, dayOfWeek: number, periodIndex: number) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }
  const firstRule = course.scheduleRules[0];
  const rule = createScheduleRule(firstRule);
  rule.dayOfWeek = clampNumber(dayOfWeek, 1, 7);
  rule.startPeriod = clampNumber(periodIndex, 1, periods.value.length || 1);
  const maxDuration = Math.max(1, (periods.value.length || 1) - rule.startPeriod + 1);
  rule.periodCount = clampNumber(firstRule?.periodCount ?? 1, 1, maxDuration);
  course.scheduleRules = [rule];
  selectedCourseId.value = course.id;
  selectedCell.value = { dayOfWeek: rule.dayOfWeek, periodIndex: rule.startPeriod };
  courseForm.value = {
    title: course.title,
    teacher: course.teacher ?? '',
    location: course.location ?? '',
    color: course.color,
    note: course.note ?? '',
    dayOfWeek: rule.dayOfWeek,
    startPeriod: rule.startPeriod,
    periodCount: rule.periodCount,
  };
  touchSpace(space);
}
function selectCourse(course: CourseTemplate) {
  const firstRule = course.scheduleRules[0];
  selectedCourseId.value = course.id;
  selectedCell.value = firstRule ? { dayOfWeek: firstRule.dayOfWeek, periodIndex: firstRule.startPeriod } : null;
  courseForm.value = {
    title: course.title,
    teacher: course.teacher ?? "",
    location: course.location ?? "",
    color: course.color,
    note: course.note ?? "",
    dayOfWeek: firstRule?.dayOfWeek ?? selectedCell.value?.dayOfWeek ?? 1,
    startPeriod: firstRule?.startPeriod ?? selectedCell.value?.periodIndex ?? 1,
    periodCount: firstRule?.periodCount ?? 1,
  };
}

function focusCourse(course: CourseTemplate) {
  selectCourse(course);
  currentView.value = "timetable";
}

function prepareNewCourse() {
  if (!selectedCell.value) {
    selectedCell.value = { dayOfWeek: visibleDayLabels.value[0]?.index ?? 1, periodIndex: periods.value[0]?.index ?? 1 };
  }
  selectedCourseId.value = null;
  courseForm.value = createBlankCourseForm(selectedCell.value);
  currentView.value = "timetable";
}

function resetCourseForm() {
  selectedCourseId.value = null;
  courseForm.value = createBlankCourseForm(selectedCell.value ?? undefined);
}

function saveCourse() {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }

  const now = new Date().toISOString();
  const existingIndex = space.courses.findIndex((course) => course.id === selectedCourseId.value);
  const existingCourse = existingIndex >= 0 ? space.courses[existingIndex] : null;
  const rule = createScheduleRule(existingCourse?.scheduleRules[0]);
  const courseId = existingCourse?.id ?? createId("course");
  const meta = createEntityMeta(courseId, existingCourse ?? undefined, now);
  const course: CourseTemplate = {
    ...meta,
    title: courseForm.value.title.trim() || "未命名课程",
    teacher: nullableText(courseForm.value.teacher),
    location: nullableText(courseForm.value.location),
    color: courseForm.value.color || colorPalette[0],
    note: nullableText(courseForm.value.note),
    tags: existingCourse?.tags ?? [],
    scheduleRules: [rule],
    reminderRules: existingCourse?.reminderRules ?? [],
  };

  if (existingIndex >= 0) {
    space.courses.splice(existingIndex, 1, course);
  } else {
    space.courses.push(course);
  }

  selectedCourseId.value = course.id;
  selectedCell.value = { dayOfWeek: rule.dayOfWeek, periodIndex: rule.startPeriod };
  touchSpace(space);
}

function deleteSelectedCourse() {
  const space = selectedSpace.value;
  const course = selectedCourse.value;
  if (!space || !course) {
    return;
  }

  space.courses = space.courses.filter((item) => item.id !== course.id);
  selectedCourseId.value = null;
  courseForm.value = createBlankCourseForm(selectedCell.value ?? undefined);
  touchSpace(space);
}

function updateSpaceName(value: string) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }
  space.name = value.trim() || "未命名课表";
  touchSpace(space);
}

function updateAcademicYear(value: string) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }
  space.academicYear = nullableText(value);
  touchSpace(space);
}

function toggleVisibleDay(dayOfWeek: number, checked: boolean) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }

  const settings = ensureSettings(space);
  const visibleDays = new Set(settings.visibleDays ?? [1, 2, 3, 4, 5]);
  if (checked) {
    visibleDays.add(dayOfWeek);
  } else if (visibleDays.size > 1) {
    visibleDays.delete(dayOfWeek);
  }
  settings.visibleDays = Array.from(visibleDays).sort((left, right) => left - right);
  settings.showWeekend = settings.visibleDays.some((day) => day >= 6);
  syncWeekViewModeFromSpace(space);
  touchSpace(space);
}

function isVisibleDay(dayOfWeek: number): boolean {
  return selectedSpace.value?.settings?.visibleDays?.includes(dayOfWeek) ?? dayOfWeek <= 5;
}

function toggleWeekend(checked: boolean) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }

  const settings = ensureSettings(space);
  const visibleDays = new Set(settings.visibleDays ?? [1, 2, 3, 4, 5]);
  if (checked) {
    visibleDays.add(6);
    visibleDays.add(7);
  } else {
    visibleDays.delete(6);
    visibleDays.delete(7);
  }
  settings.visibleDays = Array.from(visibleDays).sort((left, right) => left - right);
  settings.showWeekend = checked;
  syncWeekViewModeFromSpace(space);
  touchSpace(space);
}

function toggleCompactMode(checked: boolean) {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }
  ensureSettings(space).compactMode = checked;
  touchSpace(space);
}

function updatePeriod(periodIndex: number, field: PeriodField, value: string) {
  const space = selectedSpace.value;
  const period = space?.timeTemplates[0]?.periods.find((item) => item.index === periodIndex);
  if (!space || !period) {
    return;
  }
  period[field] = value;
  touchSpace(space);
}

function addPeriod() {
  const space = selectedSpace.value;
  if (!space) {
    return;
  }
  const template = ensureTimeTemplate(space);
  const nextIndex = template.periods.length + 1;
  template.periods.push({ index: nextIndex, label: `第 ${nextIndex} 节`, startTime: "18:00", endTime: "18:45" });
  touchSpace(space);
}

function removePeriod(periodIndex: number) {
  const space = selectedSpace.value;
  const template = space?.timeTemplates[0];
  if (!space || !template || template.periods.length <= 1) {
    return;
  }

  template.periods = template.periods
    .filter((period) => period.index !== periodIndex)
    .map((period, index) => ({ ...period, index: index + 1 }));
  space.courses.forEach((course) => {
    course.scheduleRules.forEach((rule) => {
      rule.startPeriod = Math.min(rule.startPeriod, template.periods.length);
      rule.periodCount = Math.max(1, Math.min(rule.periodCount, template.periods.length - rule.startPeriod + 1));
    });
  });
  touchSpace(space);
}

function openCreateSpaceDialog() {
  spaceForm.value = { name: "", academicYear: selectedSpace.value?.academicYear ?? "2026-2027" };
  isCreateSpaceDialogOpen.value = true;
}

function closeCreateSpaceDialog() {
  isCreateSpaceDialogOpen.value = false;
}

function createSpace() {
  const name = spaceForm.value.name.trim() || `新课表 ${spaces.value.length + 1}`;
  const academicYear = spaceForm.value.academicYear.trim() || "2026-2027";
  const space = createLocalSpace(name, academicYear);
  spaces.value.unshift(space);
  selectedSpaceId.value = space.id;
  selectedCourseId.value = null;
  selectedCell.value = { dayOfWeek: 1, periodIndex: 1 };
  courseForm.value = createBlankCourseForm(selectedCell.value);
  syncWeekViewModeFromSpace(space);
  currentView.value = "timetable";
  persistSpace(space);
  closeCreateSpaceDialog();
}

function openImportFilePicker() {
  importFileInput.value?.click();
}

function handleImportFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  importMessage.value = file ? `已选择 ${file.name}，识别流程正在开发中。` : "图片课程表导入正在开发中。";
}

function setImportMessage(message: string) {
  importMessage.value = message;
}

function setCourseModuleMessage(message: string) {
  courseModuleMessage.value = message;
}

function formatCourseSummary(course: CourseTemplate): string {
  const rule = course.scheduleRules[0];
  const schedule = rule ? `${formatDay(rule.dayOfWeek)} 第 ${rule.startPeriod} 节起` : "未排课";
  return `${course.teacher ?? "未设置教师"} · ${course.location ?? "未设置教室"} · ${schedule}`;
}

function formatDay(dayOfWeek: number): string {
  return dayOptions.find((day) => day.index === dayOfWeek)?.label ?? `第 ${dayOfWeek} 天`;
}

function formatWeekDate(dayOfWeek: number): string {
  const term = activeTerm.value;
  if (!term?.startDate) {
    return '';
  }
  const start = new Date(term.startDate);
  const weekStartDay = selectedSpace.value?.weekStartDay ?? 1;
  const offset = (dayOfWeek - weekStartDay + 7) % 7;
  start.setDate(start.getDate() + offset);
  return `${start.getMonth() + 1}月${start.getDate()}日`;
}

async function reloadSpaces() {
  loadError.value = null;
  try {
    if (!window.catclass) {
      bridgeStatus.value = "Local preview mode";
      loadError.value = "桌面桥接暂不可用，当前仍可体验本地交互界面。";
      ensureLocalSpace();
      return;
    }
    bridgeStatus.value = window.catclass.ping();
    const previousSpaceId = selectedSpaceId.value;
    const remoteSpaces = await window.catclass.listSpaces();
    spaces.value = cloneSpaces(remoteSpaces);
    selectedSpaceId.value = spaces.value.some((space) => space.id === previousSpaceId) ? previousSpaceId : spaces.value[0]?.id ?? null;
    syncWeekViewModeFromSpace(selectedSpace.value);
    selectedCell.value = { dayOfWeek: 1, periodIndex: 1 };
    courseForm.value = createBlankCourseForm(selectedCell.value);
    ensureLocalSpace();
  } catch (error) {
    bridgeStatus.value = "Local preview mode";
    loadError.value = error instanceof Error ? error.message : "无法加载课表数据，当前进入本地预览。";
    ensureLocalSpace();
  }
}

function ensureLocalSpace() {
  if (spaces.value.length > 0) {
    selectedSpaceId.value = selectedSpaceId.value ?? spaces.value[0]?.id ?? null;
    syncWeekViewModeFromSpace(selectedSpace.value);
    return;
  }

  const space = createLocalSpace("默认课表", "2026-2027");
  spaces.value = [space];
  selectedSpaceId.value = space.id;
  selectedCell.value = { dayOfWeek: 1, periodIndex: 1 };
  courseForm.value = createBlankCourseForm(selectedCell.value);
}

function createBlankCourseForm(cell?: SelectedCell): CourseFormState {
  return {
    title: "",
    teacher: "",
    location: "",
    color: colorPalette[0],
    note: "",
    dayOfWeek: cell?.dayOfWeek ?? 1,
    startPeriod: cell?.periodIndex ?? 1,
    periodCount: 1,
  };
}

function createScheduleRule(existingRule?: ScheduleRule): ScheduleRule {
  const now = new Date().toISOString();
  const maxPeriod = Math.max(periods.value.length, 1);
  const startPeriod = clampNumber(courseForm.value.startPeriod, 1, maxPeriod);
  const maxDuration = Math.max(1, maxPeriod - startPeriod + 1);
  return {
    ...createEntityMeta(existingRule?.id ?? createId("rule"), existingRule, now),
    termId: activeTerm.value?.id ?? selectedSpace.value?.activeTermId ?? "term-local",
    dayOfWeek: clampNumber(courseForm.value.dayOfWeek, 1, 7),
    startPeriod,
    periodCount: clampNumber(courseForm.value.periodCount, 1, maxDuration),
    weekMode: existingRule?.weekMode ?? "all",
    weekSet: existingRule?.weekSet ?? [],
    exceptions: existingRule?.exceptions ?? [],
  };
}

function createLocalSpace(name: string, academicYear: string): TimetableSpace {
  const termId = createId("term");
  const now = new Date().toISOString();
  const term: Term = {
    ...createEntityMeta(termId, undefined, now),
    name: "当前学期",
    startDate: "2026-09-01",
    endDate: "2027-01-15",
    totalWeeks: 20,
    timezone: "Asia/Shanghai",
  };
  return {
    ...createEntityMeta(createId("space"), undefined, now),
    name,
    academicYear,
    weekStartDay: 1,
    activeTermId: termId,
    terms: [term],
    timeTemplates: [createDefaultTimeTemplate(now)],
    courses: [],
    settings: { visibleDays: [1, 2, 3, 4, 5], showWeekend: false, compactMode: false },
  };
}

function createDefaultTimeTemplate(now: string): TimeTemplate {
  return {
    ...createEntityMeta(createId("time-template"), undefined, now),
    name: "标准节次",
    periods: [
      { index: 1, label: "第 1 节", startTime: "08:00", endTime: "08:45" },
      { index: 2, label: "第 2 节", startTime: "08:55", endTime: "09:40" },
      { index: 3, label: "第 3 节", startTime: "10:00", endTime: "10:45" },
      { index: 4, label: "第 4 节", startTime: "10:55", endTime: "11:40" },
      { index: 5, label: "第 5 节", startTime: "14:00", endTime: "14:45" },
      { index: 6, label: "第 6 节", startTime: "14:55", endTime: "15:40" },
      { index: 7, label: "第 7 节", startTime: "16:00", endTime: "16:45" },
      { index: 8, label: "第 8 节", startTime: "16:55", endTime: "17:40" },
    ],
  };
}

function ensureSettings(space: TimetableSpace): SpaceSettings {
  space.settings = space.settings ?? { visibleDays: [1, 2, 3, 4, 5], showWeekend: false, compactMode: false };
  return space.settings;
}

function ensureTimeTemplate(space: TimetableSpace): TimeTemplate {
  if (!space.timeTemplates[0]) {
    space.timeTemplates = [createDefaultTimeTemplate(new Date().toISOString())];
  }
  return space.timeTemplates[0];
}

// 所有编辑动作统一经过 touchSpace：更新时间戳、版本号，并把当前课表同步给主进程内存仓库。
function touchSpace(space: TimetableSpace) {
  space.updatedAt = new Date().toISOString();
  space.version += 1;
  persistSpace(space);
}

function persistSpace(space: TimetableSpace) {
  if (!window.catclass?.saveSpace) {
    return;
  }

  void window.catclass.saveSpace(cloneSpace(space)).catch((error) => {
    loadError.value = error instanceof Error ? error.message : "保存到桌面主进程失败，当前仅保留在页面内存。";
  });
}

function createEntityMeta<T extends EntityMeta>(id: string, existing?: T, now = new Date().toISOString()): EntityMeta {
  return {
    id,
    createdAt: existing?.createdAt ?? now,
    updatedAt: now,
    version: existing ? existing.version + 1 : 1,
  };
}

function nullableText(value: string): string | null {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function clampNumber(value: number, min: number, max: number): number {
  const safeValue = Number.isFinite(value) ? value : min;
  return Math.min(Math.max(safeValue, min), max);
}

function createId(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

function cloneSpace(space: TimetableSpace): TimetableSpace {
  return JSON.parse(JSON.stringify(space)) as TimetableSpace;
}

function cloneSpaces(remoteSpaces: TimetableSpace[]): TimetableSpace[] {
  return JSON.parse(JSON.stringify(remoteSpaces)) as TimetableSpace[];
}

onMounted(() => {
  void reloadSpaces();
});
</script>

<style scoped>
:global(*) {
  box-sizing: border-box;
}

:global(body) {
  margin: 0;
  color: #14213d;
  background: #eef2f7;
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
}

button,
input,
select,
textarea {
  font: inherit;
}

button {
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
}

.app-shell.sidebar-collapsed {
  grid-template-columns: 18px minmax(0, 1fr);
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 28px 18px;
  color: #f8fafc;
  background: #202736;
}

.sidebar.collapsed {
  padding: 28px 8px;
}

.panel-toggle {
  position: absolute;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 18px;
  min-width: 18px;
  border: 0;
  border-radius: 999px;
  color: #f8fafc;
  background: #1d4ed8;
  box-shadow: 0 6px 16px rgb(15 23 42 / 24%);
}

.sidebar-toggle {
  top: 24px;
  right: -9px;
  height: 28px;
}

.editor-toggle {
  top: 18px;
  left: -9px;
  height: 28px;
}

.sidebar-collapsed .sidebar-toggle {
  right: 0;
}

.sidebar.collapsed .brand-block,
.sidebar.collapsed .nav-list,
.sidebar.collapsed .sidebar-footer {
  display: none;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 8px;
  color: #202736;
  background: #f4c95d;
  font-weight: 800;
}

.brand-block h1,
.toolbar h2,
.panel-heading h3,
.course-card h4,
.empty-state h3,
.development-panel h3 {
  margin: 0;
}

.brand-block p,
.toolbar-meta,
.panel-heading p,
.course-card p,
.period-cell span,
.course-chip span,
.eyebrow,
.development-panel p,
.development-state span {
  margin: 4px 0 0;
  color: #64748b;
}

.nav-list {
  display: grid;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border: 0;
  border-radius: 8px;
  padding: 11px 12px;
  text-align: left;
  color: #cbd5e1;
  background: transparent;
}

.nav-item:hover,
.space-tab:hover,
.ghost-action:hover {
  background: rgb(37 99 235 / 10%);
}

.nav-item.active {
  color: #202736;
  background: #f8fafc;
}

.nav-count {
  min-width: 24px;
  border-radius: 999px;
  padding: 2px 7px;
  color: #0f766e;
  background: #ccfbf1;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
  padding: 10px 8px;
  color: #cbd5e1;
  font-size: 13px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #f97316;
}

.status-dot.online {
  background: #22c55e;
}

.workspace {
  min-width: 0;
  padding: 28px;
}

.toolbar,
.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.panel-heading-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.week-mode-toggle {
  display: inline-flex;
  gap: 0;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  overflow: hidden;
}

.segmented-button {
  border: 0;
  border-radius: 0;
  background: #ffffff;
}

.segmented-button.active {
  color: #ffffff;
  background: #2563eb;
}

.toolbar {
  margin-bottom: 18px;
}

.toolbar-actions,
.button-row,
.course-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.button-row.end {
  justify-content: flex-end;
}

.button-row.vertical {
  align-items: stretch;
  flex-direction: column;
}

.eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.primary-action,
.ghost-action,
.danger-action,
.icon-action {
  border: 0;
  border-radius: 8px;
  min-height: 40px;
  padding: 10px 14px;
}

.primary-action {
  color: #ffffff;
  background: #2563eb;
}

.ghost-action {
  color: #14213d;
  background: #ffffff;
  border: 1px solid #d9e2ef;
}

.danger-action {
  color: #991b1b;
  background: #fee2e2;
}

.ghost-action.small,
.icon-action {
  min-height: 34px;
  padding: 7px 10px;
  font-size: 13px;
}

.icon-action {
  width: 34px;
  color: #991b1b;
  background: #fee2e2;
}

.notice {
  margin-bottom: 18px;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  padding: 14px 16px;
  background: #ffffff;
}

.notice.warning {
  color: #92400e;
  border-color: #fde68a;
  background: #fffbeb;
}

.content-grid,
.module-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.content-grid.editor-collapsed {
  grid-template-columns: minmax(0, 1fr) 18px;
}

.timetable-panel,
.editor-panel,
.module-panel,
.development-panel,
.dialog {
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgb(15 23 42 / 8%);
}

.editor-panel {
  position: relative;
}

.editor-panel.collapsed {
  padding: 18px 8px;
  overflow: visible;
}

.editor-panel.collapsed > :not(.editor-toggle) {
  display: none;
}

.timetable-panel,
.editor-panel,
.module-panel,
.development-panel {
  padding: 18px;
}

.panel-heading {
  margin-bottom: 14px;
}

.panel-heading.compact {
  align-items: flex-start;
}

.panel-heading > span {
  border-radius: 999px;
  padding: 4px 10px;
  color: #0f766e;
  background: #ccfbf1;
  font-size: 12px;
  font-weight: 700;
}

.space-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
}

.space-tab {
  border: 1px solid #d9e2ef;
  border-radius: 999px;
  padding: 7px 12px;
  color: #334155;
  background: #ffffff;
  white-space: nowrap;
}

.space-tab.active {
  color: #ffffff;
  border-color: #2563eb;
  background: #2563eb;
}

.timetable-grid {
  display: grid;
  overflow: auto;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
}

.grid-corner,
.day-header,
.period-cell,
.course-cell {
  min-height: 76px;
  border-right: 1px solid #d9e2ef;
  border-bottom: 1px solid #d9e2ef;
  background: #ffffff;
}

.content-grid.compact .period-cell,
.content-grid.compact .course-cell {
  min-height: 58px;
}

.grid-corner,
.day-header {
  display: grid;
  min-height: 44px;
  place-items: center;
  color: #334155;
  background: #f8fafc;
  font-weight: 700;
}

.day-header {
  gap: 2px;
}

.day-date {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.period-cell {
  display: grid;
  align-content: center;
  gap: 4px;
  padding: 10px;
  background: #fbfdff;
}

.period-cell span,
.course-chip span {
  font-size: 12px;
}

.course-cell {
  position: relative;
  display: grid;
  align-content: start;
  gap: 6px;
  padding: 8px;
  outline: 0;
}

.course-cell.drag-over {
  background: #e0f2fe;
  box-shadow: inset 0 0 0 2px #38bdf8;
}

.course-cell:hover,
.course-cell.selected {
  background: #f0f9ff;
}

.cell-placeholder {
  display: grid;
  min-height: 42px;
  place-items: center;
  color: #94a3b8;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  font-weight: 700;
}

.course-chip {
  display: grid;
  gap: 3px;
  width: 100%;
  min-height: 48px;
  border: 1px solid;
  border-left-width: 4px;
  border-radius: 8px;
  padding: 8px;
  color: #14213d;
  text-align: left;
}

.course-chip.dragging {
  opacity: 0.55;
}

.course-chip.active {
  box-shadow: 0 0 0 2px rgb(37 99 235 / 22%);
}

.editor-form,
.settings-grid,
.settings-section,
.period-editor-list {
  display: grid;
  gap: 12px;
}

label,
.field-title {
  display: grid;
  gap: 6px;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  padding: 10px 11px;
  color: #14213d;
  background: #ffffff;
}

textarea {
  resize: vertical;
}

.form-grid.two-columns,
.settings-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.color-section {
  display: grid;
  gap: 8px;
}

.color-palette {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.color-swatch {
  width: 30px;
  height: 30px;
  border: 2px solid transparent;
  border-radius: 999px;
  padding: 0;
}

.color-swatch.selected {
  border-color: #14213d;
  box-shadow: 0 0 0 2px #ffffff inset;
}

.color-input {
  width: 42px;
  height: 34px;
  padding: 3px;
}

.course-list {
  display: grid;
  gap: 10px;
}

.course-card {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

.course-color {
  width: 10px;
  height: 100%;
  min-height: 42px;
  border-radius: 999px;
}

.empty-state,
.development-state {
  display: grid;
  justify-items: start;
  gap: 12px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  padding: 22px;
  color: #334155;
  background: #f8fafc;
}

.empty-state.inline {
  margin-top: 12px;
}

.development-panel {
  display: grid;
  gap: 14px;
}

.day-toggle-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.check-tile {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  padding: 8px;
  background: #ffffff;
}

.check-tile input,
.switch-row input {
  width: auto;
}

.two-toggles {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  padding: 12px;
}

.period-editor-row {
  display: grid;
  grid-template-columns: minmax(82px, 1fr) 92px 92px 34px;
  gap: 8px;
  align-items: center;
}

.editor-panel.collapsed .editor-toggle,
.sidebar.collapsed .sidebar-toggle {
  inset-inline: auto;
}

.period-editor-row input {
  min-width: 0;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgb(15 23 42 / 42%);
}

.dialog {
  display: grid;
  gap: 14px;
  width: min(460px, 100%);
  padding: 18px;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

@media (max-width: 1080px) {
  .content-grid,
  .module-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 780px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    flex-direction: row;
    justify-content: space-between;
    overflow-x: auto;
  }

  .nav-list {
    grid-auto-flow: column;
  }

  .sidebar-footer {
    display: none;
  }

  .toolbar,
  .panel-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .course-card {
    grid-template-columns: 10px minmax(0, 1fr);
  }

  .course-actions {
    grid-column: 2;
  }

  .form-grid.two-columns,
  .settings-grid,
  .two-toggles,
  .day-toggle-grid {
    grid-template-columns: 1fr;
  }

  .period-editor-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>

