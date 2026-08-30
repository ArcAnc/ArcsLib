const algoliaExperiencesUrl =
  'https://cdn.jsdelivr.net/npm/@algolia/experiences/dist/experiences.js?appId=9IOL8AD27F&apiKey=1998641ffb9dfd648720f98cd0dba2d0&experienceId=9IOL8AD27F&env=prod';

export default function algoliaExperiencesPlugin() {
  return {
    name: 'algolia-experiences',
    injectHtmlTags() {
      return {
        postBodyTags: [
          {
            tagName: 'script',
            attributes: {
              src: algoliaExperiencesUrl,
            },
          },
        ],
      };
    },
  };
}
