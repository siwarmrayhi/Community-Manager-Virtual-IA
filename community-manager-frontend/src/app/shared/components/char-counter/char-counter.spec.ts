import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CharCounter } from './char-counter';

describe('CharCounter', () => {
  let component: CharCounter;
  let fixture: ComponentFixture<CharCounter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CharCounter],
    }).compileComponents();

    fixture = TestBed.createComponent(CharCounter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
