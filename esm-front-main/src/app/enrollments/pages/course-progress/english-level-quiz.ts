export type QuizOptionKey = 'A' | 'B' | 'C' | 'D';

export interface EnglishQuizQuestion {
  id: number;
  text: string;
  options: Record<QuizOptionKey, string>;
  correct: QuizOptionKey;
}

// Simple built‑in English level quiz (grammar, vocab, reading)
// You can expand or replace these questions later.
export const ENGLISH_LEVEL_QUIZ: EnglishQuizQuestion[] = [
  {
    id: 1,
    text: 'I ______ from Spain.',
    options: { A: 'is', B: 'are', C: 'am', D: 'be' },
    correct: 'C'
  },
  {
    id: 2,
    text: 'This is my friend. ______ name is Oscar.',
    options: { A: 'Her', B: 'Our', C: 'Yours', D: 'His' },
    correct: 'D'
  },
  {
    id: 3,
    text: 'There ______ 20 chairs in the office.',
    options: { A: 'is', B: 'are', C: 'has', D: 'have' },
    correct: 'B'
  },
  {
    id: 4,
    text: 'Melissa enjoys comedies, but she ______ horror movies at all.',
    options: {
      A: 'isn\'t liking',
      B: 'doesn\'t like',
      C: 'not likes',
      D: 'doesn\'t likes'
    },
    correct: 'B'
  },
  {
    id: 5,
    text: "Sorry, I can't talk right now. I've ______ started my online English test.",
    options: { A: 'already', B: 'yet', C: 'just', D: 'still' },
    correct: 'C'
  },
  {
    id: 6,
    text: 'The kitchen is ______ than the bathroom.',
    options: { A: 'more big', B: 'more bigger', C: 'biggest', D: 'bigger' },
    correct: 'D'
  },
  {
    id: 7,
    text: 'We have been friends ______ many years.',
    options: { A: 'since', B: 'from', C: 'during', D: 'for' },
    correct: 'D'
  },
  {
    id: 8,
    text: 'Jack was ill last week and he ______ go out.',
    options: {
      A: "needn't",
      B: "can't",
      C: "mustn't",
      D: "couldn't"
    },
    correct: 'D'
  },
  {
    id: 9,
    text: 'If it ______ this afternoon, we will stay at home.',
    options: { A: 'raining', B: 'rains', C: 'will rain', D: 'rain' },
    correct: 'B'
  },
  {
    id: 10,
    text:
      'Take a warm coat, ______ you might get very cold outside.',
    options: { A: 'otherwise', B: 'in case', C: 'so that', D: 'in order to' },
    correct: 'B'
  },
  {
    id: 11,
    text: 'It was hard to get home last night because there weren’t ______ buses or trains running after midnight.',
    options: { A: 'much', B: 'some', C: 'any', D: 'a few' },
    correct: 'C'
  },
  {
    id: 12,
    text: 'Angelina ______ shopping every day.',
    options: { A: 'is going', B: 'go', C: 'going', D: 'goes' },
    correct: 'D'
  },
  {
    id: 13,
    text: 'They ______ in the park when it started to rain heavily.',
    options: { A: 'walked', B: 'were walking', C: 'were walk', D: 'are walking' },
    correct: 'B'
  },
  {
    id: 14,
    text: '______ seen a falling star before?',
    options: { A: 'Did you ever', B: 'Are you ever', C: 'Have you ever', D: 'Do you ever' },
    correct: 'C'
  },
  {
    id: 15,
    text: 'You ______ pay for the tickets. They’re free.',
    options: { A: 'have to', B: 'don’t have', C: 'don’t need to', D: 'doesn’t have to' },
    correct: 'C'
  },
  {
    id: 16,
    text: 'These are the photos ______ I took on holiday.',
    options: { A: 'which', B: 'who', C: 'what', D: 'where' },
    correct: 'A'
  },
  {
    id: 17,
    text: 'He doesn’t smoke now, but he ______ a lot when he was young.',
    options: { A: 'has smoked', B: 'smokes', C: 'used to smoke', D: 'was smoked' },
    correct: 'C'
  },
  {
    id: 18,
    text: 'Michael plays basketball ______ anyone else I know.',
    options: { A: 'more good than', B: 'as better as', C: 'best than', D: 'better than' },
    correct: 'D'
  },
  {
    id: 19,
    text: 'I promise I ______ you as soon as I’ve finished this cleaning.',
    options: { A: 'will help', B: 'am helping', C: 'going to help', D: 'have helped' },
    correct: 'A'
  },
  {
    id: 20,
    text: 'This town ______ by lots of tourists during the summer.',
    options: { A: 'visits', B: 'visited', C: 'is visiting', D: 'is visited' },
    correct: 'D'
  }
];

