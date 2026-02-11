import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'newlineToParagraph',
  standalone: true,
})
export class NewlineToParagraphPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) return '';

    // Split by newlines and wrap each paragraph
    return value
      .split('\n')
      .filter((line) => line.trim().length > 0)
      .map((line) => `<p>${line}</p>`)
      .join('');
  }
}
