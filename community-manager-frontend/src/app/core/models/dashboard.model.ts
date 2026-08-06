import { Network, ContentType, SubscriptionStatus } from './enums';
import { ConfigurationResponse } from './configuration.model';
import { PostResponse } from './post.model';
import { CalendarEntryResponse } from './calendar.model';

export interface StatisticsResponse {
  postsGenerated: number;
  postsValidated: number;
  validationRate: number; // 0 à 1
  mostUsedContentType: ContentType | null;
  mostUsedNetwork: Network | null;
  postsByStatus: Record<string, number>;
}

export interface DashboardResponse {
  subscriptionStatus: SubscriptionStatus;
  configuration: ConfigurationResponse | null;
  pendingValidationCount: number;
  newSuggestionsCount: number;
  nextPlannedPost: PostResponse | null;
  upcomingCalendar: CalendarEntryResponse[]; // 5 prochains jours
  statistics: StatisticsResponse;
  quickActions: string[];
}
