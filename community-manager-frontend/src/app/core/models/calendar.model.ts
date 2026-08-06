import { ContentType } from './enums';
import { PostResponse } from './post.model';

export interface CalendarEntryResponse {
  date: string; // "YYYY-MM-DD"
  postingDay: boolean;
  suggestedContentType: ContentType | null;
  posts: PostResponse[];
}
