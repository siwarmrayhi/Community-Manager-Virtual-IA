import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscribeConfirm } from './subscribe-confirm';

describe('SubscribeConfirm', () => {
  let component: SubscribeConfirm;
  let fixture: ComponentFixture<SubscribeConfirm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscribeConfirm],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscribeConfirm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
