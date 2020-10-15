import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DegradeComponent } from './degrade.component';

describe('DegradeComponent', () => {
  let component: DegradeComponent;
  let fixture: ComponentFixture<DegradeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DegradeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DegradeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
