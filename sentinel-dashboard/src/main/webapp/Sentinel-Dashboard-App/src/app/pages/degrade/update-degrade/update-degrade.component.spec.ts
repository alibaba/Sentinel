import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateDegradeComponent } from './update-degrade.component';

describe('UpdateDegradeComponent', () => {
  let component: UpdateDegradeComponent;
  let fixture: ComponentFixture<UpdateDegradeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateDegradeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateDegradeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
