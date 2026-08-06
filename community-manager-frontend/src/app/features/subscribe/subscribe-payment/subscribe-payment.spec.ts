import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscribePayment } from './subscribe-payment';

describe('SubscribePayment', () => {
  let component: SubscribePayment;
  let fixture: ComponentFixture<SubscribePayment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscribePayment],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscribePayment);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
