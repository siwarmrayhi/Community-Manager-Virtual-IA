export type Network = 'LINKEDIN' | 'FACEBOOK' | 'INSTAGRAM';
export type Tone = 'PROFESSIONAL' | 'APPROACHABLE' | 'DYNAMIC' | 'INSTITUTIONAL';
export type Language = 'FRENCH' | 'ENGLISH' | 'ARABIC' | 'MULTILINGUAL';

export type ContentType =
  | 'LISTING_PROMO'
  | 'EXPERT_ADVICE'
  | 'INDUSTRY_NEWS_COMMENTARY'
  | 'CLIENT_SUCCESS_STORY'
  | 'ENGAGING_CONTENT'
  | 'EVENT_PROMO';

export type SuggestionStatus = 'NEW' | 'USED' | 'DISMISSED';
export type SubscriptionStatus = 'PENDING' | 'ACTIVE' | 'CANCELLED';

// ⚠️ PLACEHOLDER NON CONFIRMÉ — à vérifier dans Frequency.java avant de tester le formulaire.
export type Frequency = 'THREE_POSTS_PER_WEEK' | 'ONE_POST_PER_DAY' | 'TWO_POSTS_PER_DAY';

// ⚠️ PLACEHOLDER NON CONFIRMÉ — pas encore utilisé dans l'UI actuelle.
export type PostStatus = 'GENERATED' | 'PENDING_VALIDATION' | 'READY_TO_PUBLISH' | 'REJECTED';
