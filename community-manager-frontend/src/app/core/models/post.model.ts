import { Network, ContentType, PostStatus } from './enums';

export interface PostResponse {
  id: number;
  network: Network;
  contentType: ContentType;
  status: PostStatus;
  text: string;
  imageDescription: string | null;
  hashtags: string | null;
  callToAction: string | null;
  plannedDate: string | null;
  generatedByFallback: boolean;
  generationNotice: string | null;
  createdAt: string;
  updatedAt: string;
  validatedAt: string | null;
  rejectedAt: string | null;
}

export interface PostFilters {
  type?: ContentType;
  network?: Network;
  from?: string;
  to?: string;
  status?: PostStatus;
}

export interface GeneratePostRequest {
  userEmail: string;
  networks?: Network[];
  contentType?: ContentType;
  topic?: string;
  additionalInstructions?: string;
  plannedDate?: string;
}

export interface ModifyPostRequest {
  userEmail: string;
  text: string;
}

export interface AlternativeRequest {
  userEmail: string;
  additionalInstructions?: string;
}

export interface ReusePostRequest {
  userEmail: string;
  network?: Network;
  plannedDate?: string;
  additionalInstructions?: string;
}
