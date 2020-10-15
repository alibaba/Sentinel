import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDegradeComponent } from './create-degrade.component';

describe('CreateDegradeComponent', () => {
  let component: CreateDegradeComponent;
  let fixture: ComponentFixture<CreateDegradeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateDegradeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateDegradeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
