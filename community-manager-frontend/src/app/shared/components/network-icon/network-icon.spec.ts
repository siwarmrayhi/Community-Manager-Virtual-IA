import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NetworkIcon } from './network-icon';

describe('NetworkIcon', () => {
  let component: NetworkIcon;
  let fixture: ComponentFixture<NetworkIcon>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NetworkIcon],
    }).compileComponents();

    fixture = TestBed.createComponent(NetworkIcon);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
