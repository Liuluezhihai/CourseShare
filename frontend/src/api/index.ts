import type {
  CategoryNode,
  CurrentUser,
  DiscoverResource,
  OperationLog,
  MyReviewItem,
  PageResult,
  ReviewFeed,
  ReviewItem,
  ResourceDetail,
  ResourceListItem,
  ResourceQuery,
  RoleOption,
  UserListItem
} from './types'
import { download, get, patch, post, put, remove } from './http'

function queryString(params: object): string {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value))
    }
  })
  const value = search.toString()
  return value ? `?${value}` : ''
}

export const authApi = {
  me: () => get<CurrentUser>('/auth/me'),
  login: (payload: { username: string; password: string }) => post<CurrentUser>('/auth/login', payload),
  register: (payload: {
    username: string
    password: string
    nickname: string
    email?: string
    enableAuthor?: boolean
  }) =>
    post<void>('/auth/register', payload),
  enableAuthor: () => post<CurrentUser>('/auth/author/enable'),
  logout: () => post<void>('/auth/logout')
}

export const categoryApi = {
  tree: () => get<CategoryNode[]>('/categories/tree'),
  adminTree: () => get<CategoryNode[]>('/admin/categories/tree'),
  create: (payload: Record<string, unknown>) => post<CategoryNode>('/admin/categories', payload),
  update: (id: number, payload: Record<string, unknown>) => put<CategoryNode>(`/admin/categories/${id}`, payload),
  status: (id: number, status: number, version: number) =>
    patch<void>(`/admin/categories/${id}/status`, { status, version }),
  delete: (id: number) => remove(`/admin/categories/${id}`)
}

export const resourceApi = {
  page: (params: ResourceQuery) =>
    get<PageResult<ResourceListItem>>(`/resources${queryString(params)}`),
  detail: (id: number) => get<ResourceDetail>(`/resources/${id}`),
  download: (id: number, filename: string) =>
    download(`/resources/${id}/download`, filename),
  authorPage: (params: ResourceQuery) =>
    get<PageResult<ResourceListItem>>(`/author/resources${queryString(params)}`),
  authorDetail: (id: number) => get<ResourceDetail>(`/author/resources/${id}`),
  create: (data: Record<string, unknown>, file?: File) => {
    const form = new FormData()
    form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    if (file) form.append('file', file)
    return post<ResourceDetail>('/author/resources', form)
  },
  update: (id: number, data: Record<string, unknown>, file?: File) => {
    const form = new FormData()
    form.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    if (file) form.append('file', file)
    return put<ResourceDetail>(`/author/resources/${id}`, form)
  },
  authorDelete: (id: number) => remove(`/author/resources/${id}`),
  submit: (id: number, version: number) => post<void>(`/author/resources/${id}/submit`, { version }),
  withdraw: (id: number, version: number) => post<void>(`/author/resources/${id}/withdraw`, { version }),
  offline: (id: number, version: number) => post<void>(`/author/resources/${id}/offline`, { version }),
  adminPage: (params: ResourceQuery) =>
    get<PageResult<ResourceListItem>>(`/admin/resources${queryString(params)}`),
  adminDetail: (id: number) => get<ResourceDetail>(`/admin/resources/${id}`),
  adminDownload: (id: number, filename: string) =>
    download(`/admin/resources/${id}/download`, filename),
  approve: (id: number, version: number) => post<void>(`/admin/resources/${id}/approve`, { version }),
  reject: (id: number, version: number, remark: string) =>
    post<void>(`/admin/resources/${id}/reject`, { version, remark }),
  batchPublish: (items: Array<{ id: number; version: number }>) =>
    post<number>('/admin/resources/batch-publish', { items }),
  adminOffline: (id: number, version: number) =>
    post<void>(`/admin/resources/${id}/offline`, { version }),
  adminDelete: (id: number) => remove(`/admin/resources/${id}`)
}

export const favoriteApi = {
  ids: () => get<number[]>('/me/favorite-ids'),
  page: (params: { page?: number; size?: number }) =>
    get<PageResult<ResourceListItem>>(`/me/favorites${queryString(params)}`),
  add: (id: number) => put<void>(`/resources/${id}/favorite`),
  remove: (id: number) => remove(`/resources/${id}/favorite`)
}

export const reviewApi = {
  list: (resourceId: number, params: { page?: number; size?: number }) =>
    get<ReviewFeed>(`/resources/${resourceId}/reviews${queryString(params)}`),
  save: (resourceId: number, payload: { score: number; content?: string }) =>
    put<ReviewItem>(`/resources/${resourceId}/review`, payload),
  remove: (resourceId: number) => remove(`/resources/${resourceId}/review`),
  like: (reviewId: number) => put<void>(`/reviews/${reviewId}/like`),
  unlike: (reviewId: number) => remove(`/reviews/${reviewId}/like`),
  mine: (params: { page?: number; size?: number }) =>
    get<PageResult<MyReviewItem>>(`/me/reviews${queryString(params)}`),
  visibility: (reviewId: number, visible: boolean) =>
    patch<void>(`/admin/reviews/${reviewId}/visibility`, { visible })
}

export const discoverApi = {
  page: (params: { sort: string; page?: number; size?: number }) =>
    get<PageResult<DiscoverResource>>(`/discover/resources${queryString(params)}`)
}

export const userApi = {
  page: (params: { keyword?: string; status?: number; page?: number; size?: number }) =>
    get<PageResult<UserListItem>>(`/admin/users${queryString(params)}`),
  roles: () => get<RoleOption[]>('/admin/roles'),
  assignRoles: (id: number, roleCodes: string[]) =>
    put<UserListItem>(`/admin/users/${id}/roles`, { roleCodes }),
  status: (id: number, status: number) => patch<void>(`/admin/users/${id}/status`, { status })
}

export const logApi = {
  page: (params: Record<string, unknown>) =>
    get<PageResult<OperationLog>>(`/admin/logs${queryString(params)}`)
}

