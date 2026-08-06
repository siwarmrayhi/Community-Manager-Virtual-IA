import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscribeConfig } from './subscribe-config';

describe('SubscribeConfig', () => {
  let component: SubscribeConfig;
  let fixture: ComponentFixture<SubscribeConfig>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscribeConfig],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscribeConfig);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
