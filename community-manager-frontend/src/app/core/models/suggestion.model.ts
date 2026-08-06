import { ContentType, SuggestionStatus } from './enums';

export interface SuggestionResponse {
  id: number;
  contentType: ContentType;
  title: string;
  description: string;
  suggestedHashtags: string | null;
  sourceReference: string | null;
  suggestedDate: string; // "YYYY-MM-DD"
  status: SuggestionStatus;
  createdAt: string;
}
