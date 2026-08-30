import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeneratePostDialog } from './generate-post-dialog';

describe('GeneratePostDialog', () => {
  let component: GeneratePostDialog;
  let fixture: ComponentFixture<GeneratePostDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GeneratePostDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(GeneratePostDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
