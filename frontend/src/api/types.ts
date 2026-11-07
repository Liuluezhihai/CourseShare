export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  requestId?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface CurrentUser {
  id: number
  username: string
  nickname: string
  email?: string
  roles: string[]
  permissions: string[]
}

export interface CategoryNode {
  id: number
  parentId?: number
  name: string
  level: number
  sortOrder: number
  status: number
  description?: string
  version: number
  resourceCount?: number
  children: CategoryNode[]
}

export interface ResourceListItem {
  id: number
  title: string
  summary: string
  categoryId: number
  categoryName: string
  authorId: number
  authorName: string
  attachmentName?: string
  attachmentExt?: string
  attachmentSize?: number
  status: number
  viewCount: number
  downloadCount: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
  version: number
  averageRating?: number
  ratingCount?: number
  favoriteCount?: number
  reviewLikeCount?: number
  popularScore?: number
}

export interface ResourceDetail extends ResourceListItem {
  content: string
  attachmentMime?: string
  auditBy?: number
  auditorName?: string
  auditAt?: string
  auditRemark?: string
}

export interface UserListItem {
  id: number
  username: string
  nickname: string
  email?: string
  status: number
  lastLoginAt?: string
  createdAt: string
  roleCodes: string[]
  roleNames: string[]
}

export interface RoleOption {
  id: number
  code: string
  name: string
  description?: string
}

export interface OperationLog {
  id: number
  requestId: string
  userId?: number
  usernameSnapshot?: string
  module: string
  action: string
  httpMethod: string
  uri: string
  ip?: string
  durationMs: number
  success: number
  errorCode?: string
  errorMessage?: string
  paramsJson?: string
  createdAt: string
}

export interface ResourceQuery {
  keyword?: string
  categoryId?: number
  authorId?: number
  status?: number
  page?: number
  size?: number
  sortBy?: string
  sortDirection?: string
}

export type DiscoverSort = 'latest' | 'popular' | 'rating' | 'likes'

export interface DiscoverResource extends ResourceListItem {
  averageRating: number
  ratingCount: number
  favoriteCount: number
  reviewLikeCount: number
  popularScore: number
}

export interface ReviewItem {
  id: number
  resourceId: number
  userId: number
  nickname: string
  score: number
  content?: string
  status: number
  likeCount: number
  likedByMe: boolean
  mine: boolean
  createdAt: string
  updatedAt: string
}

export interface ReviewFeed {
  reviews: PageResult<ReviewItem>
  averageScore: number
  ratingCount: number
  mine?: ReviewItem
}

export interface MyReviewItem {
  id: number
  resourceId: number
  resourceTitle: string
  resourceStatus: number
  resourceDeleted: boolean
  score: number
  content?: string
  likeCount: number
  status: number
  createdAt: string
  updatedAt: string
}

