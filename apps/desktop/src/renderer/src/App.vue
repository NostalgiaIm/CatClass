<template>
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <span class="brand-mark">C</span>
        <div>
          <h1>CatClass</h1>
          <p>Desktop Preview</p>
        </div>
      </div>

      <nav class="nav-list" aria-label="Primary">
        <button class="nav-item active" type="button">Timetable</button>
        <button class="nav-item" type="button">Courses</button>
        <button class="nav-item" type="button">Import</button>
        <button class="nav-item" type="button">Settings</button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="toolbar">
        <div>
          <p class="eyebrow">{{ bridgeStatus }}</p>
          <h2>{{ selectedSpace?.name ?? "Loading timetable" }}</h2>
        </div>
        <button class="primary-action" type="button" @click="reloadSpaces">Refresh</button>
      </header>

      <section v-if="loadError" class="notice error">
        {{ loadError }}
      </section>

      <section v-else class="content-grid">
        <article class="timetable-panel">
          <div class="panel-heading">
            <div>
              <h3>Week View</h3>
              <p>{{ activeTermLabel }}</p>
            </div>
            <span>{{ visibleDayLabels.length }} days</span>
          </div>

          <div class="timetable-grid" :style="gridStyle">
            <div class="grid-corner">Time</div>
            <div v-for="day in visibleDayLabels" :key="day.index" class="day-header">
              {{ day.label }}
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
              >
                <div
                  v-for="course in coursesForCell(day.index, period.index)"
                  :key="course.id"
                  class="course-chip"
                  :style="{ borderColor: course.color, backgroundColor: `${course.color}16` }"
                >
                  <strong>{{ course.title }}</strong>
                  <span>{{ course.location ?? "No room" }}</span>
                </div>
              </div>
            </template>
          </div>
        </article>

        <aside class="details-panel">
          <div class="panel-heading compact">
            <div>
              <h3>Courses</h3>
              <p>{{ selectedSpace?.courses.length ?? 0 }} loaded</p>
            </div>
          </div>

          <article v-for="course in selectedSpace?.courses" :key="course.id" class="course-card">
            <span class="course-color" :style="{ backgroundColor: course.color }"></span>
            <div>
              <h4>{{ course.title }}</h4>
              <p>{{ course.teacher ?? "No teacher" }} · {{ course.location ?? "No room" }}</p>
            </div>
          </article>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import type { CourseTemplate, Period, TimetableSpace } from "../../../../../packages/contracts/types/catclass";

const dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const spaces = ref<TimetableSpace[]>([]);
const loadError = ref<string | null>(null);
const bridgeStatus = ref("Connecting to desktop bridge");

const selectedSpace = computed(() => spaces.value[0] ?? null);
const activeTermLabel = computed(() => selectedSpace.value?.terms[0]?.name ?? "No active term");
const periods = computed<Period[]>(() => selectedSpace.value?.timeTemplates[0]?.periods ?? []);
const visibleDayLabels = computed(() => {
  const visibleDays = selectedSpace.value?.settings?.visibleDays ?? [1, 2, 3, 4, 5, 6, 7];
  return visibleDays.map((index) => ({ index, label: dayNames[index - 1] ?? `Day ${index}` }));
});
const gridStyle = computed(() => ({
  gridTemplateColumns: `132px repeat(${visibleDayLabels.value.length}, minmax(132px, 1fr))`,
}));

function coursesForCell(dayOfWeek: number, periodIndex: number): CourseTemplate[] {
  return selectedSpace.value?.courses.filter((course) =>
    course.scheduleRules.some((rule) => rule.dayOfWeek === dayOfWeek && rule.startPeriod === periodIndex),
  ) ?? [];
}

async function reloadSpaces() {
  loadError.value = null;
  try {
    if (!window.catclass) {
      throw new Error("CatClass preload bridge is unavailable.");
    }
    bridgeStatus.value = window.catclass.ping();
    spaces.value = await window.catclass.listSpaces();
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : "Unable to load timetable data.";
  }
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

button {
  font: inherit;
}

.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 28px 18px;
  color: #f8fafc;
  background: #172033;
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
  color: #172033;
  background: #f4c95d;
  font-weight: 800;
}

.brand-block h1,
.toolbar h2,
.panel-heading h3,
.course-card h4 {
  margin: 0;
}

.brand-block p,
.panel-heading p,
.course-card p,
.eyebrow {
  margin: 4px 0 0;
}

.brand-block p,
.nav-item,
.panel-heading p,
.course-card p,
.period-cell span,
.course-chip span,
.eyebrow {
  color: #64748b;
}

.nav-list {
  display: grid;
  gap: 8px;
}

.nav-item {
  border: 0;
  border-radius: 8px;
  padding: 11px 12px;
  text-align: left;
  color: #cbd5e1;
  background: transparent;
  cursor: default;
}

.nav-item.active {
  color: #172033;
  background: #f8fafc;
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

.toolbar {
  margin-bottom: 18px;
}

.eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.primary-action {
  border: 0;
  border-radius: 8px;
  padding: 10px 14px;
  color: #ffffff;
  background: #2563eb;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
  align-items: start;
}

.timetable-panel,
.details-panel,
.notice {
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgb(15 23 42 / 8%);
}

.timetable-panel,
.details-panel {
  padding: 18px;
}

.notice {
  padding: 16px;
}

.notice.error {
  color: #991b1b;
  border-color: #fecaca;
  background: #fff1f2;
}

.panel-heading {
  margin-bottom: 14px;
}

.panel-heading.compact {
  align-items: flex-start;
}

.panel-heading span {
  border-radius: 999px;
  padding: 4px 10px;
  color: #0f766e;
  background: #ccfbf1;
  font-size: 12px;
  font-weight: 700;
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
  min-height: 74px;
  border-right: 1px solid #d9e2ef;
  border-bottom: 1px solid #d9e2ef;
  background: #ffffff;
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

.period-cell {
  display: grid;
  align-content: center;
  gap: 4px;
  padding: 10px;
  background: #fbfdff;
}

.period-cell span {
  font-size: 12px;
}

.course-cell {
  padding: 8px;
}

.course-chip {
  display: grid;
  gap: 3px;
  min-height: 48px;
  border-left: 4px solid;
  border-radius: 8px;
  padding: 8px;
}

.course-chip + .course-chip {
  margin-top: 6px;
}

.course-chip span {
  font-size: 12px;
}

.details-panel {
  display: grid;
  gap: 10px;
}

.course-card {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
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

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    flex-direction: row;
    justify-content: space-between;
  }

  .nav-list {
    grid-auto-flow: column;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

</style>
