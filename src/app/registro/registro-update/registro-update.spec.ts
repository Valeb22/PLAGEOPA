import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistroUpdate } from './registro-update';

describe('RegistroUpdate', () => {
  let component: RegistroUpdate;
  let fixture: ComponentFixture<RegistroUpdate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroUpdate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistroUpdate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
