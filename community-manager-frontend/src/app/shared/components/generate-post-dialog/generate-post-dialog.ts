import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Network, ContentType } from '../../../core/models/enums';
import { GeneratePostRequest } from '../../../core/models/post.model';

export type GeneratePostDialogResult = Omit<GeneratePostRequest, 'userEmail'>;

@Component({
  selector: 'app-generate-post-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './generate-post-dialog.html',
  styleUrl: './generate-post-dialog.scss',
})
export class GeneratePostDialog {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<GeneratePostDialog>);

  protected readonly contentTypes: ContentType[] = [
    'LISTING_PROMO',
    'EXPERT_ADVICE',
    'INDUSTRY_NEWS_COMMENTARY',
    'CLIENT_SUCCESS_STORY',
    'ENGAGING_CONTENT',
    'EVENT_PROMO',
  ];

  private readonly contentTypeLabels: Record<string, string> = {
    LISTING_PROMO: 'Annonce & opportunité',
    EXPERT_ADVICE: "Conseil d'expert",
    INDUSTRY_NEWS_COMMENTARY: 'Actualité commentée',
    CLIENT_SUCCESS_STORY: 'Succès client',
    ENGAGING_CONTENT: 'Contenu engageant',
    EVENT_PROMO: 'Événement',
  };

  protected readonly form = this.fb.group({
    linkedin: [false],
    facebook: [false],
    instagram: [false],
    contentType: [''],
    topic: [''],
    additionalInstructions: [''],
    plannedDate: [''],
  });

  protected contentTypeLabel(t: ContentType): string {
    return this.contentTypeLabels[t];
  }

  protected onCancel(): void {
    this.dialogRef.close();
  }

  protected onGenerate(): void {
    const { linkedin, facebook, instagram, contentType, topic, additionalInstructions, plannedDate } =
      this.form.getRawValue();

    const networks: Network[] = [];
    if (linkedin) networks.push('LINKEDIN');
    if (facebook) networks.push('FACEBOOK');
    if (instagram) networks.push('INSTAGRAM');

    const result: GeneratePostDialogResult = {
      networks: networks.length > 0 ? networks : undefined,
      contentType: contentType ? (contentType as ContentType) : undefined,
      topic: topic || undefined,
      additionalInstructions: additionalInstructions || undefined,
      plannedDate: plannedDate || undefined,
    };

    this.dialogRef.close(result);
  }
}
