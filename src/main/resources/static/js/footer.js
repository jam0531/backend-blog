let startingPoint = 0
const header = document.querySelector('.banner')

header.addEventListener('mouseenter', (e) => {
  startingPoint = e.clientX
  header.classList.add('moving')
})

header.addEventListener('mouseout', (e) => {
  header.classList.remove('moving')
  header.style.setProperty('--percentage', 0.5)
})

header.addEventListener('mousemove', (e) => {
  let percentage = ((startingPoint - e.clientX) / window.outerWidth) * 2 + 0.5

  header.style.setProperty('--percentage', percentage)
})