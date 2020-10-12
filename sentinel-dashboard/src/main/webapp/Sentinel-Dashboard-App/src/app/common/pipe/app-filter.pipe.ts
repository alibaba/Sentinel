import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'appFilter'
})
export class AppFilterPipe implements PipeTransform {
  transform(value: any, ...args: any[]): any {
    var res = [];
    if (args.length && args[0] !== undefined && args[0] !== "") {
      value.map(ele => {
        if (ele.app.toLowerCase().includes(args[0].toLowerCase())) {
          res.push(ele);
        }
      });
      return res;
    } else {
      return value;
    }
  }
}
