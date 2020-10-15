import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'systemFilter'
})
export class SystemFilterPipe implements PipeTransform {

  transform(value: any, ...args: any[]): any {
    var res = [];
    if (args.length && args[0] !== null && args[0] !== undefined && args[0] !== "") {
      value.map(ele => {
        if (ele[args[0]] >= 0) {
          res.push(ele);
        }
      });
      return res;
    } else {
      return value;
    }
  }

}
