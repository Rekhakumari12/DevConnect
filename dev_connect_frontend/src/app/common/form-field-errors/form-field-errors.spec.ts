import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormFieldErrors } from './form-field-errors';

describe('FormFieldErrors', () => {
  let component: FormFieldErrors;
  let fixture: ComponentFixture<FormFieldErrors>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormFieldErrors]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormFieldErrors);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
