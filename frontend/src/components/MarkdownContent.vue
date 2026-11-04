<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const props = defineProps<{ content: string }>()

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

const defaultLinkOpen =
  markdown.renderer.rules.link_open ||
  ((tokens, index, options, env, self) => self.renderToken(tokens, index, options))

markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  const token = tokens[index]
  token.attrSet('target', '_blank')
  token.attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, index, options, env, self)
}

const rendered = computed(() => DOMPurify.sanitize(markdown.render(props.content || '')))
</script>

<template>
  <article class="markdown-body" v-html="rendered" />
</template>

<style>
.markdown-body {
  color: #273449;
  font-size: 16px;
  line-height: 1.85;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  color: var(--cs-text);
  line-height: 1.4;
  letter-spacing: -0.025em;
}

.markdown-body h1 {
  margin: 0 0 24px;
  font-size: 30px;
}

.markdown-body h2 {
  margin: 38px 0 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--cs-border);
  font-size: 23px;
}

.markdown-body h3 {
  margin: 28px 0 12px;
  font-size: 19px;
}

.markdown-body code {
  padding: 2px 6px;
  border-radius: 5px;
  background: #eff6ff;
  color: #1d4ed8;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 0.9em;
}

.markdown-body pre {
  overflow-x: auto;
  padding: 18px;
  border-radius: 10px;
  background: #0f172a;
  color: #e2e8f0;
}

.markdown-body pre code {
  padding: 0;
  background: transparent;
  color: inherit;
}

.markdown-body blockquote {
  margin: 22px 0;
  padding: 8px 18px;
  border-left: 4px solid var(--cs-primary);
  background: #f8fafc;
  color: var(--cs-muted);
}

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
}

.markdown-body th,
.markdown-body td {
  padding: 10px 12px;
  border: 1px solid var(--cs-border);
  text-align: left;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 10px;
}
</style>

