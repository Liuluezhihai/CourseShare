import axios, { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const client = axios.create({
  baseURL: `${import.meta.env.BASE_URL}api`,
  withCredentials: true,
  timeout: 20000
})

client.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const url = String(error.config?.url ?? '')
    if (status === 401) {
      window.dispatchEvent(new CustomEvent('courseshare:unauthorized'))
    }
    const silent = status === 401 && url.endsWith('/auth/me')
    if (!silent) {
      ElMessage.error(error.response?.data?.message || '请求失败，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await client.get<{ data: T }>(url, config)
  return response.data.data
}

export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await client.post<{ data: T }>(url, data, config)
  return response.data.data
}

export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await client.put<{ data: T }>(url, data, config)
  return response.data.data
}

export async function patch<T>(url: string, data?: unknown): Promise<T> {
  const response = await client.patch<{ data: T }>(url, data)
  return response.data.data
}

export async function remove(url: string): Promise<void> {
  await client.delete(url)
}

export async function download(url: string, fallbackName: string): Promise<void> {
  const response = await client.get(url, { responseType: 'blob' })
  const blob = new Blob([response.data])
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = fallbackName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}

